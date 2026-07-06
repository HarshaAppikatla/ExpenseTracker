package com.expenseflow.expense.domain.validator;

import com.expenseflow.expense.domain.entity.ExpenseEntity;
import com.expenseflow.expense.domain.entity.ExpenseParticipantEntity;
import com.expenseflow.expense.domain.entity.ExpenseSplitEntity;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Component
public class ExpenseValidator {

    public void validateInvariants(ExpenseEntity expense) {
        if (expense == null) {
            throw new IllegalArgumentException("Expense cannot be null");
        }
        if (expense.getMoney() == null || expense.getMoney().getAmount() == null) {
            throw new IllegalArgumentException("Expense money amount cannot be null");
        }
        if (expense.getPaidByUserId() == null || expense.getPaidByUserId().isBlank()) {
            throw new IllegalArgumentException("Expense paidByUserId cannot be empty");
        }
        if (expense.getSplits() == null || expense.getSplits().isEmpty()) {
            throw new IllegalArgumentException("Expense must have at least one split");
        }

        BigDecimal total = expense.getMoney().getAmount();
        BigDecimal splitSum = expense.getSplits().stream()
                .map(ExpenseSplitEntity::getOwedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (total.setScale(2, java.math.RoundingMode.HALF_UP)
                .compareTo(splitSum.setScale(2, java.math.RoundingMode.HALF_UP)) != 0) {
            throw new IllegalArgumentException("The sum of splits (" + splitSum + ") does not equal total amount (" + total + ")");
        }

        Set<String> participantUserIds = new HashSet<>();
        for (ExpenseParticipantEntity participant : expense.getParticipants()) {
            if (!participantUserIds.add(participant.getUserId())) {
                throw new IllegalArgumentException("Duplicate participant found: " + participant.getUserId());
            }
        }

        Set<String> splitUserIds = new HashSet<>();
        for (ExpenseSplitEntity split : expense.getSplits()) {
            if (!splitUserIds.add(split.getUserId())) {
                throw new IllegalArgumentException("Duplicate split found: " + split.getUserId());
            }
            if (!participantUserIds.contains(split.getUserId())) {
                throw new IllegalArgumentException("Split debtor " + split.getUserId() + " is not a participant in the expense");
            }
        }
    }
}
