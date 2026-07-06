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
import com.expenseflow.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseCommandServiceImplTest {

    @Mock
    private ExpenseRepository expenseRepository;
    @Mock
    private ExpenseMapper expenseMapper;
    @Mock
    private ExpenseValidator expenseValidator;
    @Mock
    private ExpenseBusinessRuleService expenseBusinessRuleService;
    @Mock
    private SplitCalculatorFactory splitCalculatorFactory;
    @Mock
    private com.expenseflow.expense.service.ExpenseUserEnrichmentService expenseUserEnrichmentService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ExpenseCommandServiceImpl expenseCommandService;

    private CreateExpenseRequest createRequest;
    private UpdateExpenseRequest updateRequest;
    private ExpenseEntity expenseEntity;
    private ExpenseDto expenseDto;

    @BeforeEach
    void setUp() {
        Map<String, BigDecimal> allocationValues = new LinkedHashMap<>();
        allocationValues.put("user-1", new BigDecimal("50.00"));
        allocationValues.put("user-2", new BigDecimal("50.00"));

        createRequest = new CreateExpenseRequest(
                "trip-123",
                "Dinner",
                ExpenseCategory.FOOD,
                new BigDecimal("100.00"),
                "USD",
                "user-1",
                LocalDate.now(),
                SplitType.EQUAL,
                allocationValues
        );

        updateRequest = new UpdateExpenseRequest(
                "Lunch",
                ExpenseCategory.FOOD,
                new BigDecimal("80.00"),
                "USD",
                "user-1",
                LocalDate.now(),
                SplitType.EQUAL,
                allocationValues
        );

        expenseEntity = ExpenseEntity.builder()
                .id("expense-123")
                .groupId("group-123")
                .tripId("trip-123")
                .description("Dinner")
                .category(ExpenseCategory.FOOD)
                .categoryType(ExpenseCategoryType.SYSTEM)
                .money(new Money(new BigDecimal("100.00"), CurrencyCode.USD))
                .paidByUserId("user-1")
                .createdByUserId("creator-1")
                .status(ExpenseStatus.DRAFT)
                .expenseDate(LocalDate.now())
                .participants(new ArrayList<>())
                .splits(new ArrayList<>())
                .attachments(new ArrayList<>())
                .version(1L)
                .build();

        expenseDto = new ExpenseDto(
                "expense-123",
                "group-123",
                "trip-123",
                "Dinner",
                ExpenseCategory.FOOD,
                ExpenseCategoryType.SYSTEM,
                new BigDecimal("100.00"),
                "USD",
                "user-1",
                "Payer Name",
                "creator-1",
                "Creator Name",
                ExpenseStatus.DRAFT,
                SplitType.EQUAL,
                LocalDate.now(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                1L
        );
    }

    @Test
    void createExpense_ShouldSucceed_AndPublishEvents() {
        // Arrange
        SplitCalculator mockCalculator = mock(SplitCalculator.class);
        Map<String, BigDecimal> calculatedOwed = new LinkedHashMap<>();
        calculatedOwed.put("user-1", new BigDecimal("50.00"));
        calculatedOwed.put("user-2", new BigDecimal("50.00"));

        when(splitCalculatorFactory.get(any())).thenReturn(mockCalculator);
        when(mockCalculator.calculate(any(), any(), any())).thenReturn(calculatedOwed);
        when(expenseRepository.save(any(ExpenseEntity.class))).thenReturn(expenseEntity);
        when(expenseMapper.toDto(any(ExpenseEntity.class))).thenReturn(expenseDto);

        when(expenseUserEnrichmentService.populateUserNames(any(ExpenseDto.class))).thenReturn(expenseDto);

        // Act
        ExpenseDto result = expenseCommandService.createExpense("group-123", createRequest, "creator-1");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo("expense-123");
        verify(expenseRepository).save(any(ExpenseEntity.class));
        verify(expenseValidator).validateInvariants(any(ExpenseEntity.class));

        // Verify Domain Events are published
        verify(eventPublisher).publishEvent(any(ExpenseCreatedEvent.class));
        verify(eventPublisher).publishEvent(any(SplitCalculatedEvent.class));
    }

    @Test
    void updateExpense_ShouldSucceed_AndPublishEvents() {
        // Arrange
        SplitCalculator mockCalculator = mock(SplitCalculator.class);
        Map<String, BigDecimal> calculatedOwed = new LinkedHashMap<>();
        calculatedOwed.put("user-1", new BigDecimal("50.00"));
        calculatedOwed.put("user-2", new BigDecimal("50.00"));

        when(expenseRepository.findByIdAndIsDeletedFalse(eq("expense-123"))).thenReturn(Optional.of(expenseEntity));
        when(splitCalculatorFactory.get(any())).thenReturn(mockCalculator);
        when(mockCalculator.calculate(any(), any(), any())).thenReturn(calculatedOwed);
        when(expenseRepository.save(any(ExpenseEntity.class))).thenReturn(expenseEntity);
        when(expenseMapper.toDto(any(ExpenseEntity.class))).thenReturn(expenseDto);

        when(expenseUserEnrichmentService.populateUserNames(any(ExpenseDto.class))).thenReturn(expenseDto);

        // Act
        ExpenseDto result = expenseCommandService.updateExpense("expense-123", updateRequest, "creator-1");

        // Assert
        assertThat(result).isNotNull();
        verify(expenseRepository).save(expenseEntity);
        
        verify(eventPublisher).publishEvent(any(ExpenseUpdatedEvent.class));
        verify(eventPublisher).publishEvent(any(SplitCalculatedEvent.class));
    }

    @Test
    void deleteExpense_ShouldSetDeletedAndPublishEvent() {
        // Arrange
        when(expenseRepository.findByIdAndIsDeletedFalse(eq("expense-123"))).thenReturn(Optional.of(expenseEntity));

        // Act
        expenseCommandService.deleteExpense("expense-123", "creator-1");

        // Assert
        assertThat(expenseEntity.isDeleted()).isTrue();
        assertThat(expenseEntity.getDeletedBy()).isEqualTo("creator-1");
        verify(expenseRepository).save(expenseEntity);
        verify(eventPublisher).publishEvent(any(ExpenseDeletedEvent.class));
    }

    @Test
    void transitionStatus_Posted_ShouldPublishExpensePostedEvent() {
        // Arrange
        when(expenseRepository.findByIdAndIsDeletedFalse(eq("expense-123"))).thenReturn(Optional.of(expenseEntity));
        when(expenseRepository.save(any(ExpenseEntity.class))).thenReturn(expenseEntity);
        when(expenseMapper.toDto(any(ExpenseEntity.class))).thenReturn(expenseDto);

        when(expenseUserEnrichmentService.populateUserNames(any(ExpenseDto.class))).thenReturn(expenseDto);

        // Act
        ExpenseDto result = expenseCommandService.transitionStatus("expense-123", ExpenseStatus.POSTED, "creator-1");

        // Assert
        assertThat(expenseEntity.getStatus()).isEqualTo(ExpenseStatus.POSTED);
        verify(eventPublisher).publishEvent(any(ExpensePostedEvent.class));
    }

    @Test
    void transitionStatus_InvalidTransition_ShouldThrowException() {
        // Arrange
        ExpenseEntity voidExpense = ExpenseEntity.builder()
                .id("expense-123")
                .groupId("group-123")
                .status(ExpenseStatus.VOID)
                .build();
        when(expenseRepository.findByIdAndIsDeletedFalse(eq("expense-123"))).thenReturn(Optional.of(voidExpense));

        // Act & Assert
        assertThatThrownBy(() -> expenseCommandService.transitionStatus("expense-123", ExpenseStatus.POSTED, "creator-1"))
                .isInstanceOf(com.expenseflow.expense.exception.InvalidExpenseStateException.class);
    }
}
