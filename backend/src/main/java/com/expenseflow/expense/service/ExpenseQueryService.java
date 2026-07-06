package com.expenseflow.expense.service;

import com.expenseflow.expense.dto.ExpenseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ExpenseQueryService {

    ExpenseDto getExpenseDetails(String expenseId, String currentUserId);

    Page<ExpenseDto> getGroupExpenses(String groupId, String tripId, String currentUserId, Pageable pageable);

    Page<ExpenseDto> getUserExpenses(String userId, Pageable pageable);
}
