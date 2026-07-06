package com.expenseflow.trip.domain.validator;

import com.expenseflow.group.service.GroupQueryService;
import com.expenseflow.trip.domain.entity.TripEntity;
import com.expenseflow.trip.domain.entity.TripParticipantEntity;
import com.expenseflow.trip.domain.valueobject.TripParticipantStatus;
import com.expenseflow.trip.exception.InvalidTripStateException;
import com.expenseflow.trip.exception.PermissionDeniedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TripBusinessRuleService {

    private final GroupQueryService groupQueryService;

    public void verifyGroupActive(String groupId) {
        if (!groupQueryService.isGroupActive(groupId)) {
            throw new InvalidTripStateException("Parent group is not active or archived");
        }
    }

    public void verifyUserIsGroupMember(String groupId, String userId) {
        if (!groupQueryService.isUserGroupMember(groupId, userId)) {
            throw new PermissionDeniedException("User must be an active member of the parent group");
        }
    }

    public void verifyOrganizerIsGroupMember(String groupId, String organizerId) {
        if (!groupQueryService.isUserGroupMember(groupId, organizerId)) {
            throw new InvalidTripStateException("Organizer must be an active member of the parent group");
        }
    }

    public void verifyUserCanManageTrip(TripEntity trip, String currentUserId) {
        if (!trip.getOrganizerId().equals(currentUserId)) {
            throw new PermissionDeniedException("Only the trip organizer can perform this operation");
        }
    }

    public void verifyUserCanInvite(TripEntity trip, String currentUserId) {
        // Invite permissions: either organizer or is a joined participant and settings allow it
        boolean isOrganizer = trip.getOrganizerId().equals(currentUserId);
        boolean isParticipant = trip.getParticipants().stream()
                .anyMatch(p -> p.getUserId().equals(currentUserId) && p.getStatus() == TripParticipantStatus.ACCEPTED);

        if (!isOrganizer && !(isParticipant && trip.getSettings().isAllowInvites())) {
            throw new PermissionDeniedException("You do not have permission to invite participants to this trip");
        }
    }
}
