package com.expenseflow.settlement.domain.engine;

import com.expenseflow.expense.domain.valueobject.CurrencyCode;
import com.expenseflow.expense.domain.valueobject.Money;
import com.expenseflow.settlement.domain.model.SettlementPair;
import com.expenseflow.settlement.domain.model.UserBalance;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Domain Service/Engine — Debt Minimization Solver (ADR-005 §10.2)
 *
 * Uses a greedy algorithm to compute the minimum number of payment transactions
 * required to zero all member balances in a group or trip.
 *
 * Resides in the domain engine package to prevent layered architecture conflicts
 * with application services.
 */
@Component
public class DebtMinimizationSolver {

    private static final BigDecimal CENT_TOLERANCE = new BigDecimal("0.01");

    public List<SettlementPair> solve(List<UserBalance> balances, CurrencyCode currency) {
        PriorityQueue<UserBalance> creditors = new PriorityQueue<>(
                (b1, b2) -> b2.money().getAmount().compareTo(b1.money().getAmount())
        );
        PriorityQueue<UserBalance> debtors = new PriorityQueue<>(
                (b1, b2) -> b2.absMoney().getAmount().compareTo(b1.absMoney().getAmount())
        );

        for (UserBalance b : balances) {
            if (b.money().getCurrency() != currency) {
                throw new IllegalArgumentException("Currency mismatch in balances: expected " + currency + ", but found " + b.money().getCurrency());
            }

            if (b.isCreditor()) creditors.offer(b);
            else if (b.isDebtor()) debtors.offer(b);
        }

        List<SettlementPair> pairs = new ArrayList<>();

        while (!creditors.isEmpty() && !debtors.isEmpty()) {
            UserBalance creditor = creditors.poll();
            UserBalance debtor   = debtors.poll();

            BigDecimal creditorAmt = creditor.money().getAmount();
            BigDecimal debtorAmtAbs = debtor.absMoney().getAmount();

            BigDecimal settleAmountVal = creditorAmt.min(debtorAmtAbs)
                    .setScale(2, RoundingMode.HALF_UP);

            Money settleMoney = new Money(settleAmountVal, currency);

            pairs.add(new SettlementPair(debtor.userId(), creditor.userId(), settleMoney));

            BigDecimal creditorResidual = creditorAmt.subtract(settleAmountVal);
            BigDecimal debtorResidual   = debtor.money().getAmount().add(settleAmountVal);

            if (creditorResidual.compareTo(CENT_TOLERANCE) > 0) {
                creditors.offer(new UserBalance(creditor.userId(), new Money(creditorResidual, currency)));
            }
            if (debtorResidual.negate().compareTo(CENT_TOLERANCE) > 0) {
                debtors.offer(new UserBalance(debtor.userId(), new Money(debtorResidual, currency)));
            }
        }

        return pairs;
    }
}
