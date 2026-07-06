package com.expenseflow.trip.domain.repository;

import com.expenseflow.trip.domain.entity.TripEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TripRepository extends JpaRepository<TripEntity, String>, JpaSpecificationExecutor<TripEntity> {

    Optional<TripEntity> findByIdAndIsDeletedFalse(String id);

    boolean existsByIdAndIsDeletedFalse(String id);

    @Query("SELECT t FROM TripEntity t WHERE t.groupId = :groupId AND t.isDeleted = false")
    Page<TripEntity> findActiveTripsByGroupId(@Param("groupId") String groupId, Pageable pageable);
}
