package com.expenseflow.expense.service.impl;

import com.expenseflow.dto.user.UserDto;
import com.expenseflow.expense.dto.*;
import com.expenseflow.expense.service.ExpenseUserEnrichmentService;
import com.expenseflow.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseUserEnrichmentServiceImpl implements ExpenseUserEnrichmentService {

    private final UserService userService;

    @Override
    public ExpenseDto populateUserNames(ExpenseDto dto) {
        if (dto == null) return null;
        return populateUserNames(Collections.singletonList(dto)).get(0);
    }

    @Override
    public List<ExpenseDto> populateUserNames(List<ExpenseDto> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return dtos;
        }
        Set<String> userIds = new HashSet<>();
        for (ExpenseDto dto : dtos) {
            userIds.add(dto.paidByUserId());
            userIds.add(dto.createdByUserId());
            if (dto.participants() != null) {
                for (ExpenseParticipantDto p : dto.participants()) {
                    userIds.add(p.userId());
                }
            }
            if (dto.splits() != null) {
                for (ExpenseSplitDto s : dto.splits()) {
                    userIds.add(s.userId());
                }
            }
        }

        List<UserDto> users = userService.getUsersByIds(new ArrayList<>(userIds));
        Map<String, UserDto> userMap = users.stream()
            .collect(Collectors.toMap(UserDto::getId, Function.identity(), (a, b) -> a));

        return dtos.stream()
            .map(dto -> {
                UserDto paidBy = userMap.get(dto.paidByUserId());
                UserDto createdBy = userMap.get(dto.createdByUserId());

                List<ExpenseParticipantDto> resolvedParticipants = null;
                if (dto.participants() != null) {
                    resolvedParticipants = dto.participants().stream()
                            .map(p -> {
                                UserDto u = userMap.get(p.userId());
                                return new ExpenseParticipantDto(
                                    p.id(),
                                    p.userId(),
                                    u != null ? u.getFullName() : "Unknown User",
                                    u != null ? u.getEmail() : ""
                                );
                            }).collect(Collectors.toList());
                }

                List<ExpenseSplitDto> resolvedSplits = null;
                if (dto.splits() != null) {
                    resolvedSplits = dto.splits().stream()
                            .map(s -> {
                                UserDto u = userMap.get(s.userId());
                                return new ExpenseSplitDto(
                                    s.id(),
                                    s.userId(),
                                    u != null ? u.getFullName() : "Unknown User",
                                    u != null ? u.getEmail() : "",
                                    s.owedAmount(),
                                    s.allocationValue()
                                );
                            }).collect(Collectors.toList());
                }

                return new ExpenseDto(
                        dto.id(),
                        dto.groupId(),
                        dto.tripId(),
                        dto.description(),
                        dto.category(),
                        dto.categoryType(),
                        dto.amount(),
                        dto.currency(),
                        dto.paidByUserId(),
                        paidBy != null ? paidBy.getFullName() : "Unknown User",
                        dto.createdByUserId(),
                        createdBy != null ? createdBy.getFullName() : "Unknown User",
                        dto.status(),
                        dto.splitType(),
                        dto.expenseDate(),
                        resolvedParticipants,
                        resolvedSplits,
                        dto.attachments(),
                        dto.version()
                );
            }).collect(Collectors.toList());
    }
}
