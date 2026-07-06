package com.expenseflow.integration;

import com.expenseflow.BaseIntegrationTest;
import com.expenseflow.trip.domain.entity.TripEntity;
import com.expenseflow.trip.domain.repository.TripRepository;
import com.expenseflow.trip.domain.valueobject.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.time.LocalDate;
import java.util.ArrayList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class TripConcurrencyIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private TripRepository tripRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private TransactionTemplate transactionTemplate;
    private String tripId;

    @BeforeEach
    void setUp() {
        transactionTemplate = new TransactionTemplate(transactionManager);

        // Delete all trips to clean up
        transactionTemplate.executeWithoutResult(status -> {
            tripRepository.deleteAll();
        });

        // Setup a trip
        tripId = transactionTemplate.execute(status -> {
            TripEntity trip = TripEntity.builder()
                    .groupId("group-1")
                    .title("Paris Concurrency")
                    .destination(new Destination("Paris", "France", "Paris, France"))
                    .schedule(new TripSchedule(LocalDate.now(), LocalDate.now().plusDays(5)))
                    .organizerId("user-1")
                    .status(TripStatus.PLANNING)
                    .settings(new TripSettings())
                    .participants(new ArrayList<>())
                    .build();
            return tripRepository.save(trip).getId();
        });
    }

    @Test
    void testOptimisticLocking_ShouldThrowException_WhenConcurrentUpdatesOccur() {
        // Load the first copy of the trip
        TripEntity tripCopy1 = transactionTemplate.execute(status -> tripRepository.findById(tripId).orElseThrow());
        
        // Load the second copy of the trip
        TripEntity tripCopy2 = transactionTemplate.execute(status -> tripRepository.findById(tripId).orElseThrow());

        // Assert they have the same initial version
        assertThat(tripCopy1.getVersion()).isEqualTo(tripCopy2.getVersion());

        // Update and save the first copy
        transactionTemplate.executeWithoutResult(status -> {
            tripCopy1.updateMetadata("Title updated by User A", tripCopy1.getDescription(), tripCopy1.getDestination(), tripCopy1.getSchedule(), tripCopy1.getSettings(), "PRESET", "cover-sunset");
            tripRepository.save(tripCopy1);
        });

        // Update the second copy (stale version) and attempt to save it
        assertThatThrownBy(() -> {
            transactionTemplate.executeWithoutResult(status -> {
                tripCopy2.updateMetadata("Title updated by User B", tripCopy2.getDescription(), tripCopy2.getDestination(), tripCopy2.getSchedule(), tripCopy2.getSettings(), "PRESET", "cover-sunset");
                tripRepository.save(tripCopy2);
            });
        }).isInstanceOf(OptimisticLockingFailureException.class);
    }
}
