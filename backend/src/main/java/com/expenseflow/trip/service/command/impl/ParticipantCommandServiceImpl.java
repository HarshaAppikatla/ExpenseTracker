package com.expenseflow.trip.service.command.impl;

import com.expenseflow.trip.domain.entity.TripActivityEntity;
import com.expenseflow.trip.domain.entity.TripEntity;
import com.expenseflow.trip.domain.repository.TripActivityRepository;
import com.expenseflow.trip.domain.repository.TripRepository;
import com.expenseflow.trip.domain.validator.TripBusinessRuleService;
import com.expenseflow.trip.domain.validator.TripValidator;
import com.expenseflow.trip.dto.TripDto;
import com.expenseflow.trip.domain.valueobject.TripActivityType;
import com.expenseflow.trip.domain.event.*;
import com.expenseflow.trip.exception.TripNotFoundException;
import com.expenseflow.trip.mapper.TripMapper;
import com.expenseflow.trip.service.command.ParticipantCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ParticipantCommandServiceImpl implements ParticipantCommandService {

    private final TripRepository tripRepository;
    private final TripActivityRepository tripActivityRepository;
    private final TripMapper tripMapper;
    private final TripValidator tripValidator;
    private final TripBusinessRuleService tripBusinessRuleService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public TripDto inviteParticipant(String tripId, String userId, String currentUserId) {
        TripEntity trip = fetchTrip(tripId);
        tripBusinessRuleService.verifyGroupActive(trip.getGroupId());
        tripBusinessRuleService.verifyUserIsGroupMember(trip.getGroupId(), userId);
        tripBusinessRuleService.verifyUserCanInvite(trip, currentUserId);

        trip.inviteParticipant(userId);

        tripValidator.validateInvariants(trip);
        TripEntity savedTrip = tripRepository.save(trip);

        logActivity(savedTrip, TripActivityType.PARTICIPANT_INVITED, currentUserId, userId, "Participant was invited to the trip");

        eventPublisher.publishEvent(new ParticipantInvitedEvent(this, savedTrip.getId(), userId, currentUserId));

        return tripMapper.toDto(savedTrip);
    }

    @Override
    public TripDto acceptInvite(String tripId, String currentUserId) {
        TripEntity trip = fetchTrip(tripId);
        tripBusinessRuleService.verifyGroupActive(trip.getGroupId());
        tripBusinessRuleService.verifyUserIsGroupMember(trip.getGroupId(), currentUserId);

        trip.acceptInvite(currentUserId);

        tripValidator.validateInvariants(trip);
        TripEntity savedTrip = tripRepository.save(trip);

        logActivity(savedTrip, TripActivityType.PARTICIPANT_ACCEPTED, currentUserId, null, "Participant joined the trip");

        eventPublisher.publishEvent(new ParticipantJoinedEvent(this, savedTrip.getId(), currentUserId));

        return tripMapper.toDto(savedTrip);
    }

    @Override
    public TripDto declineInvite(String tripId, String currentUserId) {
        TripEntity trip = fetchTrip(tripId);
        tripBusinessRuleService.verifyGroupActive(trip.getGroupId());

        trip.declineInvite(currentUserId);

        tripValidator.validateInvariants(trip);
        TripEntity savedTrip = tripRepository.save(trip);

        logActivity(savedTrip, TripActivityType.PARTICIPANT_DECLINED, currentUserId, null, "Participant declined invitation");

        return tripMapper.toDto(savedTrip);
    }

    @Override
    public TripDto leaveTrip(String tripId, String currentUserId) {
        TripEntity trip = fetchTrip(tripId);
        tripBusinessRuleService.verifyGroupActive(trip.getGroupId());

        trip.leave(currentUserId);

        tripValidator.validateInvariants(trip);
        TripEntity savedTrip = tripRepository.save(trip);

        logActivity(savedTrip, TripActivityType.PARTICIPANT_LEFT, currentUserId, null, "Participant left the trip");

        eventPublisher.publishEvent(new ParticipantRemovedEvent(this, savedTrip.getId(), currentUserId, currentUserId));

        return tripMapper.toDto(savedTrip);
    }

    @Override
    public TripDto removeParticipant(String tripId, String userId, String currentUserId) {
        TripEntity trip = fetchTrip(tripId);
        tripBusinessRuleService.verifyGroupActive(trip.getGroupId());
        tripBusinessRuleService.verifyUserCanManageTrip(trip, currentUserId);

        trip.removeParticipant(userId);

        tripValidator.validateInvariants(trip);
        TripEntity savedTrip = tripRepository.save(trip);

        logActivity(savedTrip, TripActivityType.PARTICIPANT_REMOVED, currentUserId, userId, "Participant was removed from the trip");

        eventPublisher.publishEvent(new ParticipantRemovedEvent(this, savedTrip.getId(), userId, currentUserId));

        return tripMapper.toDto(savedTrip);
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
