package com.expenseflow.settlement.service.impl;

import com.expenseflow.dto.user.UserDto;
import com.expenseflow.service.UserService;
import com.expenseflow.settlement.domain.entity.SettlementEntity;
import com.expenseflow.settlement.domain.valueobject.SettlementStatus;
import com.expenseflow.settlement.dto.SettlementResponse;
import com.expenseflow.settlement.dto.SettlementSummaryResponse;
import com.expenseflow.settlement.exception.SettlementNotFoundException;
import com.expenseflow.settlement.mapper.SettlementMapper;
import com.expenseflow.settlement.repository.SettlementRepository;
import com.expenseflow.settlement.service.SettlementBusinessRuleService;
import com.expenseflow.settlement.service.SettlementQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SettlementQueryServiceImpl implements SettlementQueryService {

    private final SettlementRepository settlementRepository;
    private final SettlementMapper settlementMapper;
    private final SettlementBusinessRuleService businessRuleService;
    private final UserService userService;

    @Override
    public SettlementSummaryResponse getSettlementSummary(String groupId, String currentUserId) {
        businessRuleService.verifyUserIsGroupMember(groupId, currentUserId);

        List<SettlementEntity> entities = settlementRepository.findByGroupIdAndStatusAndIsDeletedFalse(groupId, SettlementStatus.PENDING);
        List<SettlementEntity> disputed = settlementRepository.findByGroupIdAndStatusAndIsDeletedFalse(groupId, SettlementStatus.DISPUTED);

        List<SettlementEntity> activeSettlements = new ArrayList<>();
        activeSettlements.addAll(entities);
        activeSettlements.addAll(disputed);

        List<SettlementResponse> responses = enrichUserNames(
                activeSettlements.stream()
                        .map(settlementMapper::toResponse)
                        .collect(Collectors.toList())
        );

        BigDecimal totalOutstanding = responses.stream()
                .map(SettlementResponse::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String currency = responses.isEmpty() ? "INR" : responses.get(0).currency();

        return new SettlementSummaryResponse(
                groupId,
                null,
                currency,
                totalOutstanding,
                responses.size(),
                responses
        );
    }

    @Override
    public Page<SettlementResponse> getSettlementsByTrip(String groupId, String tripId, String currentUserId, Pageable pageable) {
        businessRuleService.verifyUserIsGroupMember(groupId, currentUserId);

        Page<SettlementEntity> page = settlementRepository.findByGroupIdAndTripIdAndIsDeletedFalse(groupId, tripId, pageable);
        List<SettlementResponse> mapped = page.getContent().stream()
                .map(settlementMapper::toResponse)
                .collect(Collectors.toList());

        return new PageImpl<>(enrichUserNames(mapped), pageable, page.getTotalElements());
    }

    @Override
    public List<SettlementResponse> getMySettlements(String groupId, String currentUserId) {
        businessRuleService.verifyUserIsGroupMember(groupId, currentUserId);

        List<SettlementEntity> entities = settlementRepository.findByGroupIdAndUserId(groupId, currentUserId);
        List<SettlementResponse> mapped = entities.stream()
                .map(settlementMapper::toResponse)
                .collect(Collectors.toList());

        return enrichUserNames(mapped);
    }

    @Override
    public SettlementResponse getSettlementById(String settlementId, String currentUserId) {
        SettlementEntity entity = settlementRepository.findByIdAndIsDeletedFalse(settlementId)
                .orElseThrow(() -> new SettlementNotFoundException(settlementId));

        businessRuleService.verifyUserIsGroupMember(entity.getGroupId(), currentUserId);

        return enrichUserNames(Collections.singletonList(settlementMapper.toResponse(entity))).get(0);
    }

    // Helper method to enrich fromUserId and toUserId with display names
    private List<SettlementResponse> enrichUserNames(List<SettlementResponse> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return dtos;
        }

        Set<String> userIds = new HashSet<>();
        for (SettlementResponse dto : dtos) {
            userIds.add(dto.fromUserId());
            userIds.add(dto.toUserId());
        }

        List<UserDto> users = userService.getUsersByIds(new ArrayList<>(userIds));
        Map<String, UserDto> userMap = users.stream()
                .collect(Collectors.toMap(UserDto::getId, Function.identity(), (a, b) -> a));

        return dtos.stream()
                .map(dto -> {
                    UserDto fromUser = userMap.get(dto.fromUserId());
                    UserDto toUser = userMap.get(dto.toUserId());
                    return new SettlementResponse(
                            dto.id(),
                            dto.groupId(),
                            dto.tripId(),
                            dto.fromUserId(),
                            fromUser != null ? fromUser.getFullName() : "Unknown User",
                            dto.toUserId(),
                            toUser != null ? toUser.getFullName() : "Unknown User",
                            dto.amount(),
                            dto.currency(),
                            dto.status(),
                            dto.settledAt(),
                            dto.createdAt(),
                            dto.updatedAt()
                    );
                }).collect(Collectors.toList());
    }
}
