package com.expenseflow.expense.service;

import com.expenseflow.expense.domain.valueobject.ExpenseStatus;
import com.expenseflow.expense.domain.valueobject.StorageProvider;
import com.expenseflow.expense.dto.CreateExpenseRequest;
import com.expenseflow.expense.dto.ExpenseDto;
import com.expenseflow.expense.dto.UpdateExpenseRequest;

public interface ExpenseCommandService {

    ExpenseDto createExpense(String groupId, CreateExpenseRequest request, String currentUserId);

    ExpenseDto updateExpense(String expenseId, UpdateExpenseRequest request, String currentUserId);

    void deleteExpense(String expenseId, String currentUserId);

    ExpenseDto transitionStatus(String expenseId, ExpenseStatus status, String currentUserId);

    ExpenseDto addAttachment(String expenseId, String url, String fileName, long fileSize, String fileType, StorageProvider provider, String currentUserId);

    ExpenseDto removeAttachment(String expenseId, String attachmentId, String currentUserId);
}
