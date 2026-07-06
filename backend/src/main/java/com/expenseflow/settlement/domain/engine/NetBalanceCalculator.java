package com.expenseflow.settlement.domain.engine;

import com.expenseflow.expense.domain.valueobject.Money;
import com.expenseflow.settlement.domain.model.UserBalance;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Domain Service/Engine — Net Balance Calculator (ADR-005 §10.1)
 *
 * Computes each member's net financial position within a group or trip
 * by scanning all POSTED expense splits.
 *
 * Resides in the domain engine package to prevent layered architecture conflicts
 * with application services.
 */
@Component
public class NetBalanceCalculator {

    public List<UserBalance> calculate(List<SplitRecord> splits) {
        Map<String, Money> balanceMap = new HashMap<>();

        for (SplitRecord split : splits) {
            Money owed = split.owedAmount();
            Money negativeOwed = new Money(owed.getAmount().negate(), owed.getCurrency());

            balanceMap.merge(split.payerUserId(), owed, Money::add);
            balanceMap.merge(split.participantUserId(), negativeOwed, Money::add);
        }

        List<UserBalance> result = new ArrayList<>();
        for (Map.Entry<String, Money> entry : balanceMap.entrySet()) {
            Money netBalance = entry.getValue();
            if (netBalance.getAmount().compareTo(BigDecimal.ZERO) != 0) {
                result.add(new UserBalance(entry.getKey(), netBalance));
            }
        }
        return result;
    }

    public record SplitRecord(
            String payerUserId,
            String participantUserId,
            Money owedAmount
    ) {}
}
