package com.expenseflow.trip.service.query.impl;

import com.expenseflow.dto.user.UserDto;
import com.expenseflow.service.UserService;
import com.expenseflow.trip.domain.entity.TripActivityEntity;
import com.expenseflow.trip.domain.entity.TripEntity;
import com.expenseflow.trip.domain.repository.TripActivityRepository;
import com.expenseflow.trip.domain.repository.TripRepository;
import com.expenseflow.trip.domain.validator.TripBusinessRuleService;
import com.expenseflow.trip.dto.TripActivityDto;
import com.expenseflow.trip.exception.TripNotFoundException;
import com.expenseflow.trip.mapper.TripMapper;
import com.expenseflow.trip.service.query.TimelineQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TimelineQueryServiceImpl implements TimelineQueryService {

    private final TripRepository tripRepository;
    private final TripActivityRepository tripActivityRepository;
    private final TripMapper tripMapper;
    private final TripBusinessRuleService tripBusinessRuleService;
    private final UserService userService;

    @Override
    public Page<TripActivityDto> getTripTimeline(String tripId, String currentUserId, Pageable pageable) {
        TripEntity trip = tripRepository.findByIdAndIsDeletedFalse(tripId)
                .orElseThrow(() -> new TripNotFoundException("Trip not found with ID: " + tripId));

        tripBusinessRuleService.verifyGroupActive(trip.getGroupId());
        tripBusinessRuleService.verifyUserIsGroupMember(trip.getGroupId(), currentUserId);

        Page<TripActivityEntity> activityPage = tripActivityRepository
                .findByTripIdOrderByOccurredAtDesc(tripId, pageable);

        if (activityPage.isEmpty()) {
            return Page.empty(pageable);
        }

        // Collect all actor and target user IDs for batch fetching
        Set<String> userIds = new HashSet<>();
        for (TripActivityEntity act : activityPage.getContent()) {
            userIds.add(act.getActorUserId());
            if (act.getTargetUserId() != null) {
                userIds.add(act.getTargetUserId());
            }
        }

        List<UserDto> profiles = userService.getUsersByIds(new ArrayList<>(userIds));
        Map<String, UserDto> profileMap = profiles.stream()
                .collect(Collectors.toMap(UserDto::getId, Function.identity(), (a, b) -> a));

        return activityPage.map(activity -> {
            TripActivityDto dto = tripMapper.toActivityDto(activity);
            
            UserDto actorProfile = profileMap.get(activity.getActorUserId());
            String actorName = actorProfile != null ? actorProfile.getFullName() : "Unknown";

            String targetName = null;
            if (activity.getTargetUserId() != null) {
                UserDto targetProfile = profileMap.get(activity.getTargetUserId());
                targetName = targetProfile != null ? targetProfile.getFullName() : "Unknown";
            }

            return new TripActivityDto(
                    dto.id(),
                    dto.tripId(),
                    dto.activityType(),
                    dto.actorUserId(),
                    actorName,
                    dto.targetUserId(),
                    targetName,
                    dto.message(),
                    dto.metadataJson(),
                    dto.occurredAt()
            );
        });
    }
}
