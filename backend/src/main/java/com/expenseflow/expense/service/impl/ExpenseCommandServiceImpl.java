package com.expenseflow.expense.service.impl;

import com.expenseflow.dto.user.UserDto;
import com.expenseflow.expense.domain.entity.ExpenseEntity;
import com.expenseflow.expense.domain.event.*;
import com.expenseflow.expense.domain.repository.ExpenseRepository;
import com.expenseflow.expense.domain.strategy.SplitCalculator;
import com.expenseflow.expense.domain.strategy.SplitCalculatorFactory;
import com.expenseflow.expense.domain.validator.ExpenseBusinessRuleService;
import com.expenseflow.expense.domain.validator.ExpenseValidator;
import com.expenseflow.expense.domain.valueobject.*;
import com.expenseflow.expense.dto.*;
import com.expenseflow.expense.exception.ExpenseNotFoundException;
import com.expenseflow.expense.mapper.ExpenseMapper;
import com.expenseflow.expense.service.ExpenseCommandService;
import com.expenseflow.expense.service.ExpenseUserEnrichmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ExpenseCommandServiceImpl implements ExpenseCommandService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseMapper expenseMapper;
    private final ExpenseValidator expenseValidator;
    private final ExpenseBusinessRuleService expenseBusinessRuleService;
    private final SplitCalculatorFactory splitCalculatorFactory;
    private final ExpenseUserEnrichmentService expenseUserEnrichmentService;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    public ExpenseDto createExpense(String groupId, CreateExpenseRequest request, String currentUserId) {
        expenseBusinessRuleService.verifyGroupActive(groupId);
        expenseBusinessRuleService.verifyUserIsGroupMember(groupId, currentUserId);
        expenseBusinessRuleService.verifyUserIsGroupMember(groupId, request.paidByUserId());
        expenseBusinessRuleService.verifyTripAssociatedWithGroup(request.tripId(), groupId, currentUserId);

        CurrencyCode currency = CurrencyCode.valueOf(request.currency().toUpperCase());
        Money money = new Money(request.amount(), currency);

        // Resolve split calculator and compute splits
        SplitCalculator calculator = splitCalculatorFactory.get(request.splitType());
        
        // Ensure all split debtors are group members
        Map<String, BigDecimal> allocationValues = request.allocationValues();
        for (String debtorId : allocationValues.keySet()) {
            expenseBusinessRuleService.verifyUserIsGroupMember(groupId, debtorId);
        }

        Map<String, BigDecimal> owedAmounts = calculator.calculate(request.amount(), allocationValues, currency);

        ExpenseEntity expense = ExpenseEntity.builder()
                .id(UUID.randomUUID().toString())
                .groupId(groupId)
                .tripId(request.tripId())
                .description(request.description())
                .category(request.category())
                .categoryType(ExpenseCategoryType.SYSTEM)
                .money(money)
                .paidByUserId(request.paidByUserId())
                .createdByUserId(currentUserId)
                .status(ExpenseStatus.DRAFT)
                .splitType(request.splitType())
                .expenseDate(request.expenseDate())
                .participants(new ArrayList<>())
                .splits(new ArrayList<>())
                .attachments(new ArrayList<>())
                .createdBy(currentUserId)
                .updatedBy(currentUserId)
                .build();

        expense.updateSplitsAndParticipants(owedAmounts, allocationValues);
        expenseValidator.validateInvariants(expense);

        ExpenseEntity savedExpense = expenseRepository.save(expense);

        // Domain events published only after successful persistence
        eventPublisher.publishEvent(new ExpenseCreatedEvent(
                this,
                savedExpense.getId(),
                savedExpense.getGroupId(),
                savedExpense.getTripId(),
                savedExpense.getMoney().getAmount(),
                savedExpense.getMoney().getCurrency().name(),
                savedExpense.getPaidByUserId()
        ));
        eventPublisher.publishEvent(new SplitCalculatedEvent(
                this,
                savedExpense.getId(),
                request.splitType(),
                owedAmounts
        ));

        return expenseUserEnrichmentService.populateUserNames(expenseMapper.toDto(savedExpense));
    }

    @Override
    public ExpenseDto updateExpense(String expenseId, UpdateExpenseRequest request, String currentUserId) {
        ExpenseEntity expense = fetchExpense(expenseId);
        expenseBusinessRuleService.verifyGroupActive(expense.getGroupId());
        expenseBusinessRuleService.verifyUserCanManageExpense(expense, currentUserId);
        expenseBusinessRuleService.verifyUserIsGroupMember(expense.getGroupId(), request.paidByUserId());

        CurrencyCode currency = CurrencyCode.valueOf(request.currency().toUpperCase());
        Money money = new Money(request.amount(), currency);

        // Resolve split calculator and compute splits
        SplitCalculator calculator = splitCalculatorFactory.get(request.splitType());
        
        Map<String, BigDecimal> allocationValues = request.allocationValues();
        for (String debtorId : allocationValues.keySet()) {
            expenseBusinessRuleService.verifyUserIsGroupMember(expense.getGroupId(), debtorId);
        }

        Map<String, BigDecimal> owedAmounts = calculator.calculate(request.amount(), allocationValues, currency);

        expense.updateMetadata(
                request.description(),
                request.category(),
                expense.getCategoryType(),
                request.expenseDate(),
                request.paidByUserId(),
                money,
                request.splitType()
        );
        expense.updateSplitsAndParticipants(owedAmounts, allocationValues);
        expense.touch(currentUserId);

        expenseValidator.validateInvariants(expense);
        ExpenseEntity savedExpense = expenseRepository.save(expense);

        eventPublisher.publishEvent(new ExpenseUpdatedEvent(
                this,
                savedExpense.getId(),
                savedExpense.getGroupId(),
                savedExpense.getMoney().getAmount(),
                savedExpense.getMoney().getCurrency().name(),
                currentUserId
        ));
        eventPublisher.publishEvent(new SplitCalculatedEvent(
                this,
                savedExpense.getId(),
                request.splitType(),
                owedAmounts
        ));

        return expenseUserEnrichmentService.populateUserNames(expenseMapper.toDto(savedExpense));
    }

    @Override
    public void deleteExpense(String expenseId, String currentUserId) {
        ExpenseEntity expense = fetchExpense(expenseId);
        expenseBusinessRuleService.verifyGroupActive(expense.getGroupId());
        expenseBusinessRuleService.verifyUserCanManageExpense(expense, currentUserId);

        expense.markDeleted(currentUserId);
        expenseRepository.save(expense);

        eventPublisher.publishEvent(new ExpenseDeletedEvent(this, expense.getId(), expense.getGroupId(), currentUserId));
    }

    @Override
    public ExpenseDto transitionStatus(String expenseId, ExpenseStatus status, String currentUserId) {
        ExpenseEntity expense = fetchExpense(expenseId);
        expenseBusinessRuleService.verifyGroupActive(expense.getGroupId());
        expenseBusinessRuleService.verifyUserCanManageExpense(expense, currentUserId);

        ExpenseStatus oldStatus = expense.getStatus();
        try {
            expense.transitionTo(status);
        } catch (IllegalStateException e) {
            throw new com.expenseflow.expense.exception.InvalidExpenseStateException(e.getMessage());
        }
        expense.touch(currentUserId);

        expenseValidator.validateInvariants(expense);
        ExpenseEntity savedExpense = expenseRepository.save(expense);

        if (status == ExpenseStatus.POSTED) {
            eventPublisher.publishEvent(new ExpensePostedEvent(
                    this,
                    savedExpense.getId(),
                    savedExpense.getGroupId(),
                    savedExpense.getMoney().getAmount(),
                    savedExpense.getMoney().getCurrency().name(),
                    currentUserId
            ));
        }

        return expenseUserEnrichmentService.populateUserNames(expenseMapper.toDto(savedExpense));
    }

    @Override
    public ExpenseDto addAttachment(String expenseId, String url, String fileName, long fileSize, String fileType, StorageProvider provider, String currentUserId) {
        ExpenseEntity expense = fetchExpense(expenseId);
        expenseBusinessRuleService.verifyGroupActive(expense.getGroupId());
        expenseBusinessRuleService.verifyUserCanManageExpense(expense, currentUserId);

        expense.addAttachment(UUID.randomUUID().toString(), url, fileName, fileSize, fileType, provider, currentUserId);
        expense.touch(currentUserId);

        expenseValidator.validateInvariants(expense);
        ExpenseEntity savedExpense = expenseRepository.save(expense);

        return expenseUserEnrichmentService.populateUserNames(expenseMapper.toDto(savedExpense));
    }

    @Override
    public ExpenseDto removeAttachment(String expenseId, String attachmentId, String currentUserId) {
        ExpenseEntity expense = fetchExpense(expenseId);
        expenseBusinessRuleService.verifyGroupActive(expense.getGroupId());
        expenseBusinessRuleService.verifyUserCanManageExpense(expense, currentUserId);

        expense.removeAttachment(attachmentId);
        expense.touch(currentUserId);

        expenseValidator.validateInvariants(expense);
        ExpenseEntity savedExpense = expenseRepository.save(expense);

        return expenseUserEnrichmentService.populateUserNames(expenseMapper.toDto(savedExpense));
    }

    private ExpenseEntity fetchExpense(String id) {
        return expenseRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ExpenseNotFoundException("Expense not found with ID: " + id));
    }

    // populateUserNames has been refactored to ExpenseUserEnrichmentService
}
