package com.expenseflow.trip.service;

import com.expenseflow.trip.domain.entity.TripActivityEntity;
import com.expenseflow.trip.domain.entity.TripEntity;
import com.expenseflow.trip.domain.repository.TripActivityRepository;
import com.expenseflow.trip.domain.repository.TripRepository;
import com.expenseflow.trip.domain.validator.TripBusinessRuleService;
import com.expenseflow.trip.domain.validator.TripValidator;
import com.expenseflow.trip.domain.valueobject.*;
import com.expenseflow.trip.dto.*;
import com.expenseflow.trip.domain.event.TripCreatedEvent;
import com.expenseflow.trip.domain.event.TripUpdatedEvent;
import com.expenseflow.trip.domain.event.TripDeletedEvent;
import com.expenseflow.trip.domain.event.TripStartedEvent;
import com.expenseflow.trip.mapper.TripMapper;
import com.expenseflow.trip.service.command.impl.TripCommandServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TripCommandServiceTest {

    @Mock
    private TripRepository tripRepository;
    @Mock
    private TripActivityRepository tripActivityRepository;
    @Mock
    private TripMapper tripMapper;
    @Mock
    private TripValidator tripValidator;
    @Mock
    private TripBusinessRuleService tripBusinessRuleService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TripCommandServiceImpl tripCommandService;

    private CreateTripRequest createRequest;
    private TripEntity tripEntity;
    private TripDto tripDto;

    @BeforeEach
    void setUp() {
        DestinationDto destDto = new DestinationDto("Paris", "France", "Paris, France");
        TripScheduleDto schedDto = new TripScheduleDto(LocalDate.now(), LocalDate.now().plusDays(5));
        createRequest = new CreateTripRequest(
                "group-1",
                "Paris Getaway",
                "Trip to Paris",
                destDto,
                schedDto,
                new TripSettingsDto("GROUP", true, false),
                "PRESET",
                "cover-1"
        );

        Destination dest = new Destination("Paris", "France", "Paris, France");
        TripSchedule sched = new TripSchedule(LocalDate.now(), LocalDate.now().plusDays(5));
        tripEntity = TripEntity.builder()
                .id("trip-1")
                .groupId("group-1")
                .title("Paris Getaway")
                .description("Trip to Paris")
                .destination(dest)
                .schedule(sched)
                .organizerId("user-1")
                .status(TripStatus.PLANNING)
                .settings(new TripSettings())
                .participants(new ArrayList<>())
                .build();

        tripDto = new TripDto(
                "trip-1",
                "group-1",
                "Paris Getaway",
                "Trip to Paris",
                destDto,
                "PRESET",
                "cover-1",
                schedDto,
                "user-1",
                "User Name",
                "PLANNING",
                new TripSettingsDto("GROUP", true, false),
                0L,
                new ArrayList<>(),
                null,
                null
        );
    }

    @Test
    void createTrip_ShouldSucceed() {
        when(tripMapper.toDestination(any())).thenReturn(tripEntity.getDestination());
        when(tripMapper.toSchedule(any())).thenReturn(tripEntity.getSchedule());
        when(tripRepository.save(any(TripEntity.class))).thenReturn(tripEntity);
        when(tripMapper.toDto(any(TripEntity.class))).thenReturn(tripDto);

        TripDto result = tripCommandService.createTrip(createRequest, "user-1");

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("trip-1");

        verify(tripBusinessRuleService).verifyGroupActive("group-1");
        verify(tripBusinessRuleService).verifyOrganizerIsGroupMember("group-1", "user-1");
        verify(tripValidator).validateInvariants(any(TripEntity.class));
        verify(tripRepository).save(any(TripEntity.class));
        verify(tripActivityRepository).save(any(TripActivityEntity.class));
        verify(eventPublisher).publishEvent(any(TripCreatedEvent.class));
    }

    @Test
    void updateTrip_ShouldSucceed() {
        UpdateTripRequest updateRequest = new UpdateTripRequest(
                "Updated Paris",
                "Updated desc",
                createRequest.destination(),
                createRequest.schedule(),
                createRequest.settings(),
                "PRESET",
                "cover-1"
        );

        when(tripRepository.findByIdAndIsDeletedFalse("trip-1")).thenReturn(Optional.of(tripEntity));
        when(tripMapper.toSchedule(any())).thenReturn(tripEntity.getSchedule());
        when(tripMapper.toDestination(any())).thenReturn(tripEntity.getDestination());
        when(tripRepository.save(any(TripEntity.class))).thenReturn(tripEntity);
        when(tripMapper.toDto(any(TripEntity.class))).thenReturn(tripDto);

        TripDto result = tripCommandService.updateTrip("trip-1", updateRequest, "user-1");

        assertThat(result).isNotNull();
        verify(tripBusinessRuleService).verifyUserCanManageTrip(tripEntity, "user-1");
        verify(tripRepository).save(tripEntity);
        verify(eventPublisher).publishEvent(any(TripUpdatedEvent.class));
    }

    @Test
    void updateStatus_ShouldSucceed_AndPublishStartedEvent() {
        when(tripRepository.findByIdAndIsDeletedFalse("trip-1")).thenReturn(Optional.of(tripEntity));
        when(tripRepository.save(any(TripEntity.class))).thenReturn(tripEntity);
        when(tripMapper.toDto(any(TripEntity.class))).thenReturn(tripDto);

        TripDto result = tripCommandService.updateStatus("trip-1", "ACTIVE", "user-1");

        assertThat(result).isNotNull();
        assertThat(tripEntity.getStatus()).isEqualTo(TripStatus.ACTIVE);
        verify(eventPublisher).publishEvent(any(TripStartedEvent.class));
    }

    @Test
    void deleteTrip_ShouldSucceed_AndPublishDeletedEvent() {
        when(tripRepository.findByIdAndIsDeletedFalse("trip-1")).thenReturn(Optional.of(tripEntity));

        tripCommandService.deleteTrip("trip-1", "user-1");

        assertThat(tripEntity.isDeleted()).isTrue();
        verify(tripRepository).save(tripEntity);
        verify(eventPublisher).publishEvent(any(TripDeletedEvent.class));
    }
}
