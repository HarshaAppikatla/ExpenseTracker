package com.expenseflow.trip.service.query.impl;

import com.expenseflow.dto.user.UserDto;
import com.expenseflow.service.UserService;
import com.expenseflow.trip.domain.entity.TripEntity;
import com.expenseflow.trip.domain.entity.TripParticipantEntity;
import com.expenseflow.trip.domain.repository.TripRepository;
import com.expenseflow.trip.domain.validator.TripBusinessRuleService;
import com.expenseflow.trip.dto.TripDto;
import com.expenseflow.trip.dto.TripParticipantDto;
import com.expenseflow.trip.exception.TripNotFoundException;
import com.expenseflow.trip.mapper.TripMapper;
import com.expenseflow.trip.service.query.TripQueryService;
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
public class TripQueryServiceImpl implements TripQueryService {

    private final TripRepository tripRepository;
    private final TripMapper tripMapper;
    private final TripBusinessRuleService tripBusinessRuleService;
    private final UserService userService;

    @Override
    public TripDto getTripDetails(String tripId, String currentUserId) {
        TripEntity trip = tripRepository.findByIdAndIsDeletedFalse(tripId)
                .orElseThrow(() -> new TripNotFoundException("Trip not found with ID: " + tripId));
        
        tripBusinessRuleService.verifyGroupActive(trip.getGroupId());
        tripBusinessRuleService.verifyUserIsGroupMember(trip.getGroupId(), currentUserId);

        // Fetch involved users to resolve names/emails
        Set<String> userIds = new HashSet<>();
        userIds.add(trip.getOrganizerId());
        trip.getParticipants().forEach(p -> userIds.add(p.getUserId()));

        List<UserDto> profiles = userService.getUsersByIds(new ArrayList<>(userIds));
        Map<String, UserDto> profileMap = profiles.stream()
                .collect(Collectors.toMap(UserDto::getId, Function.identity(), (a, b) -> a));

        return mapToDtoWithProfiles(trip, profileMap);
    }

    @Override
    public Page<TripDto> getGroupTrips(String groupId, String currentUserId, Pageable pageable) {
        tripBusinessRuleService.verifyGroupActive(groupId);
        tripBusinessRuleService.verifyUserIsGroupMember(groupId, currentUserId);

        Page<TripEntity> tripPage = tripRepository.findActiveTripsByGroupId(groupId, pageable);
        if (tripPage.isEmpty()) {
            return Page.empty(pageable);
        }

        // Collect all user IDs in page content to batch-fetch names
        Set<String> userIds = new HashSet<>();
        for (TripEntity t : tripPage.getContent()) {
            userIds.add(t.getOrganizerId());
            t.getParticipants().forEach(p -> userIds.add(p.getUserId()));
        }

        List<UserDto> profiles = userService.getUsersByIds(new ArrayList<>(userIds));
        Map<String, UserDto> profileMap = profiles.stream()
                .collect(Collectors.toMap(UserDto::getId, Function.identity(), (a, b) -> a));

        return tripPage.map(trip -> mapToDtoWithProfiles(trip, profileMap));
    }

    private TripDto mapToDtoWithProfiles(TripEntity trip, Map<String, UserDto> profileMap) {
        TripDto dto = tripMapper.toDto(trip);

        // Resolve organizer name
        UserDto organizerProfile = profileMap.get(trip.getOrganizerId());
        String organizerName = organizerProfile != null ? organizerProfile.getFullName() : "Unknown";

        // Resolve participants list names and emails
        List<TripParticipantDto> participantDtos = new ArrayList<>();
        for (TripParticipantEntity pEntity : trip.getParticipants()) {
            TripParticipantDto pDto = tripMapper.toParticipantDto(pEntity);
            UserDto uProfile = profileMap.get(pEntity.getUserId());
            String name = uProfile != null ? uProfile.getFullName() : "Unknown";
            String email = uProfile != null ? uProfile.getEmail() : "";
            
            pDto = new TripParticipantDto(
                    pDto.id(),
                    pDto.userId(),
                    name,
                    email,
                    pDto.status(),
                    pDto.joinedAt(),
                    pDto.leftAt()
            );
            participantDtos.add(pDto);
        }

        return new TripDto(
                dto.id(),
                dto.groupId(),
                dto.title(),
                dto.description(),
                dto.destination(),
                dto.coverType(),
                dto.coverImage(),
                dto.schedule(),
                dto.organizerId(),
                organizerName,
                dto.status(),
                dto.settings(),
                dto.version(),
                participantDtos,
                trip.getCreatedAt(),
                trip.getUpdatedAt()
        );
    }
}
