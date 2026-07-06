package com.expenseflow.expense.dto;

import com.expenseflow.expense.domain.valueobject.ExpenseCategory;
import com.expenseflow.expense.domain.valueobject.ExpenseCategoryType;
import com.expenseflow.expense.domain.valueobject.ExpenseStatus;
import com.expenseflow.expense.domain.valueobject.SplitType;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ExpenseDto(
    String id,
    String groupId,
    String tripId,
    String description,
    ExpenseCategory category,
    ExpenseCategoryType categoryType,
    BigDecimal amount,
    String currency,
    String paidByUserId,
    String paidByUserName,
    String createdByUserId,
    String createdByUserName,
    ExpenseStatus status,
    SplitType splitType,
    LocalDate expenseDate,
    List<ExpenseParticipantDto> participants,
    List<ExpenseSplitDto> splits,
    List<ExpenseAttachmentDto> attachments,
    Long version
) {}
