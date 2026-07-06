package com.expenseflow.dashboard.dto;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(
    BigDecimal openingBalance,
    BigDecimal totalIncome,
    BigDecimal totalExpenses,
    BigDecimal netBalance,
    List<CategoryBreakdownDto> categoryBreakdown,
    List<TransactionDto> recentTransactions
) {}
