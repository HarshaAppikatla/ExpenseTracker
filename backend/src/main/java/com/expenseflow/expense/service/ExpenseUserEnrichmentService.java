package com.expenseflow.expense.service;

import com.expenseflow.expense.dto.ExpenseDto;
import java.util.List;

public interface ExpenseUserEnrichmentService {
    ExpenseDto populateUserNames(ExpenseDto dto);
    List<ExpenseDto> populateUserNames(List<ExpenseDto> dtos);
}
