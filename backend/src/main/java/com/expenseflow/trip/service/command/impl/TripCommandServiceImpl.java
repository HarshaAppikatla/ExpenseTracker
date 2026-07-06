package com.expenseflow.trip.service.command.impl;

import com.expenseflow.trip.domain.entity.TripActivityEntity;
import com.expenseflow.trip.domain.entity.TripEntity;
import com.expenseflow.trip.domain.entity.TripParticipantEntity;
import com.expenseflow.trip.domain.repository.TripActivityRepository;
import com.expenseflow.trip.domain.repository.TripRepository;
import com.expenseflow.trip.domain.validator.TripBusinessRuleService;
import com.expenseflow.trip.domain.validator.TripValidator;
import com.expenseflow.trip.domain.valueobject.*;
import com.expenseflow.trip.dto.CreateTripRequest;
import com.expenseflow.trip.dto.TripDto;
import com.expenseflow.trip.dto.UpdateTripRequest;
import com.expenseflow.trip.domain.event.*;
import com.expenseflow.trip.exception.TripNotFoundException;
import com.expenseflow.trip.mapper.TripMapper;
import com.expenseflow.trip.service.command.TripCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TripCommandServiceImpl implements TripCommandService {

    private final TripRepository tripRepository;
    private final TripActivityRepository tripActivityRepository;
    private final TripMapper tripMapper;
    private final TripValidator tripValidator;
    private final TripBusinessRuleService tripBusinessRuleService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public TripDto createTrip(CreateTripRequest request, String currentUserId) {
        tripBusinessRuleService.verifyGroupActive(request.groupId());
        tripBusinessRuleService.verifyOrganizerIsGroupMember(request.groupId(), currentUserId);

        TripSettings settings = request.settings() != null ?
                tripMapper.toSettings(request.settings()) : new TripSettings();

        TripEntity trip = TripEntity.builder()
                .id(UUID.randomUUID().toString())
                .groupId(request.groupId())
                .title(request.title())
                .description(request.description())
                .destination(tripMapper.toDestination(request.destination()))
                .schedule(tripMapper.toSchedule(request.schedule()))
                .organizerId(currentUserId)
                .status(TripStatus.PLANNING)
                .settings(settings)
                .coverType(request.coverType() != null ? request.coverType() : "PRESET")
                .coverImage(request.coverImage())
                .participants(new ArrayList<>())
                .build();

        // Add organizer as participant in ACCEPTED status
        TripParticipantEntity organizer = TripParticipantEntity.builder()
                .id(UUID.randomUUID().toString())
                .trip(trip)
                .userId(currentUserId)
                .status(TripParticipantStatus.ACCEPTED)
                .joinedAt(LocalDateTime.now())
                .build();
        trip.getParticipants().add(organizer);

        tripValidator.validateInvariants(trip);
        TripEntity savedTrip = tripRepository.save(trip);

        logActivity(savedTrip, TripActivityType.CREATED, currentUserId, null, "Trip '" + savedTrip.getTitle() + "' was created");
        
        eventPublisher.publishEvent(new TripCreatedEvent(this, savedTrip.getId(), savedTrip.getTitle(), savedTrip.getGroupId(), savedTrip.getOrganizerId()));

        return tripMapper.toDto(savedTrip);
    }

    @Override
    public TripDto updateTrip(String tripId, UpdateTripRequest request, String currentUserId) {
        TripEntity trip = fetchTrip(tripId);
        tripBusinessRuleService.verifyGroupActive(trip.getGroupId());
        tripBusinessRuleService.verifyUserCanManageTrip(trip, currentUserId);

        TripSchedule schedule = tripMapper.toSchedule(request.schedule());
        Destination destination = tripMapper.toDestination(request.destination());
        TripSettings settings = tripMapper.toSettings(request.settings());

        trip.updateMetadata(
                request.title(),
                request.description(),
                destination,
                schedule,
                settings,
                request.coverType(),
                request.coverImage()
        );

        tripValidator.validateInvariants(trip);
        TripEntity savedTrip = tripRepository.save(trip);

        logActivity(savedTrip, TripActivityType.UPDATED, currentUserId, null, "Trip details were updated");

        eventPublisher.publishEvent(new TripUpdatedEvent(this, savedTrip.getId(), savedTrip.getTitle(), currentUserId));

        return tripMapper.toDto(savedTrip);
    }

    @Override
    public TripDto updateStatus(String tripId, String statusName, String currentUserId) {
        TripEntity trip = fetchTrip(tripId);
        tripBusinessRuleService.verifyGroupActive(trip.getGroupId());
        tripBusinessRuleService.verifyUserCanManageTrip(trip, currentUserId);

        TripStatus newStatus = TripStatus.valueOf(statusName.toUpperCase());
        TripStatus oldStatus = trip.getStatus();
        trip.transitionTo(newStatus);

        tripValidator.validateInvariants(trip);
        TripEntity savedTrip = tripRepository.save(trip);

        TripActivityType activityType = TripActivityType.UPDATED;
        com.expenseflow.core.event.ApplicationEvent event = null;

        if (newStatus == TripStatus.ACTIVE) {
            activityType = TripActivityType.STARTED;
            event = new TripStartedEvent(this, savedTrip.getId(), currentUserId);
        } else if (newStatus == TripStatus.COMPLETED) {
            activityType = TripActivityType.COMPLETED;
            event = new TripCompletedEvent(this, savedTrip.getId(), currentUserId);
        } else if (newStatus == TripStatus.CANCELLED) {
            activityType = TripActivityType.CANCELLED;
            event = new TripCancelledEvent(this, savedTrip.getId(), currentUserId);
        }

        logActivity(savedTrip, activityType, currentUserId, null, "Trip status was transitioned from " + oldStatus + " to " + newStatus);

        if (event != null) {
            eventPublisher.publishEvent(event);
        }

        return tripMapper.toDto(savedTrip);
    }

    @Override
    public void deleteTrip(String tripId, String currentUserId) {
        TripEntity trip = fetchTrip(tripId);
        tripBusinessRuleService.verifyGroupActive(trip.getGroupId());
        tripBusinessRuleService.verifyUserCanManageTrip(trip, currentUserId);

        trip.setDeleted(true);
        trip.setDeletedAt(LocalDateTime.now());
        trip.setDeletedBy(currentUserId);
        tripRepository.save(trip);

        logActivity(trip, TripActivityType.DELETED, currentUserId, null, "Trip was soft-deleted");

        eventPublisher.publishEvent(new TripDeletedEvent(this, trip.getId(), currentUserId));
    }

    private TripEntity fetchTrip(String id) {
        return tripRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new TripNotFoundException("Trip not found with ID: " + id));
    }

    private void logActivity(TripEntity trip, TripActivityType type, String actorId, String targetId, String message) {
        TripActivityEntity activity = TripActivityEntity.builder()
                .id(UUID.randomUUID().toString())
                .trip(trip)
                .activityType(type)
                .actorUserId(actorId)
                .targetUserId(targetId)
                .message(message)
                .occurredAt(LocalDateTime.now())
                .createdBy(actorId)
                .build();
        tripActivityRepository.save(activity);
    }
}
