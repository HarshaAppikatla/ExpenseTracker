package com.expenseflow.trip.domain.repository;

import com.expenseflow.trip.domain.entity.TripActivityEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TripActivityRepository extends JpaRepository<TripActivityEntity, String> {

    @Query("SELECT ta FROM TripActivityEntity ta WHERE ta.trip.id = :tripId ORDER BY ta.occurredAt DESC")
    Page<TripActivityEntity> findByTripIdOrderByOccurredAtDesc(@Param("tripId") String tripId, Pageable pageable);
}
