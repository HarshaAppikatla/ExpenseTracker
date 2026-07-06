package com.expenseflow.expense.service.impl;

import com.expenseflow.expense.domain.entity.ExpenseEntity;
import com.expenseflow.expense.domain.repository.ExpenseRepository;
import com.expenseflow.expense.domain.validator.ExpenseBusinessRuleService;
import com.expenseflow.expense.dto.ExpenseDto;
import com.expenseflow.expense.exception.ExpenseNotFoundException;
import com.expenseflow.expense.mapper.ExpenseMapper;
import com.expenseflow.expense.service.ExpenseQueryService;
import com.expenseflow.expense.service.ExpenseUserEnrichmentService;
import com.expenseflow.expense.specification.ExpenseSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpenseQueryServiceImpl implements ExpenseQueryService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper expenseMapper;
    private final ExpenseBusinessRuleService expenseBusinessRuleService;
    private final ExpenseUserEnrichmentService expenseUserEnrichmentService;

    @Override
    public ExpenseDto getExpenseDetails(String expenseId, String currentUserId) {
        ExpenseEntity expense = expenseRepository.findByIdAndIsDeletedFalse(expenseId)
                .orElseThrow(() -> new ExpenseNotFoundException("Expense not found with ID: " + expenseId));

        expenseBusinessRuleService.verifyUserIsGroupMember(expense.getGroupId(), currentUserId);

        return expenseUserEnrichmentService.populateUserNames(expenseMapper.toDto(expense));
    }

    @Override
    public Page<ExpenseDto> getGroupExpenses(String groupId, String tripId, String currentUserId, Pageable pageable) {
        expenseBusinessRuleService.verifyGroupActive(groupId);
        expenseBusinessRuleService.verifyUserIsGroupMember(groupId, currentUserId);

        Page<ExpenseEntity> entityPage;
        if (tripId != null && !tripId.isBlank()) {
            expenseBusinessRuleService.verifyTripAssociatedWithGroup(tripId, groupId, currentUserId);
            entityPage = expenseRepository.findByGroupIdAndTripIdAndIsDeletedFalse(groupId, tripId, pageable);
        } else {
            entityPage = expenseRepository.findByGroupIdAndIsDeletedFalse(groupId, pageable);
        }

        List<ExpenseDto> dtoList = entityPage.getContent().stream()
                .map(expenseMapper::toDto)
                .collect(Collectors.toList());

        List<ExpenseDto> resolvedList = expenseUserEnrichmentService.populateUserNames(dtoList);

        return new PageImpl<>(resolvedList, pageable, entityPage.getTotalElements());
    }

    @Override
    public Page<ExpenseDto> getUserExpenses(String userId, Pageable pageable) {
        Specification<ExpenseEntity> spec = Specification
                .where(ExpenseSpecification.notDeleted())
                .and(ExpenseSpecification.involvingUser(userId));

        Page<ExpenseEntity> entityPage = expenseRepository.findAll(spec, pageable);
        List<ExpenseDto> dtoList = entityPage.getContent().stream()
                .map(expenseMapper::toDto)
                .collect(Collectors.toList());

        List<ExpenseDto> resolvedList = expenseUserEnrichmentService.populateUserNames(dtoList);

        return new PageImpl<>(resolvedList, pageable, entityPage.getTotalElements());
    }
}
