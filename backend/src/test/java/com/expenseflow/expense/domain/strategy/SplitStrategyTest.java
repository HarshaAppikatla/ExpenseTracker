package com.expenseflow.expense.domain.strategy;

import com.expenseflow.expense.domain.valueobject.CurrencyCode;
import com.expenseflow.expense.domain.valueobject.SplitType;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SplitStrategyTest {

    private final SplitCalculatorFactory factory = new SplitCalculatorFactory();

    @Test
    void factory_ShouldResolveCorrectCalculators() {
        assertThat(factory.get(SplitType.EQUAL)).isInstanceOf(EqualSplitCalculator.class);
        assertThat(factory.get(SplitType.EXACT)).isInstanceOf(ExactSplitCalculator.class);
        assertThat(factory.get(SplitType.PERCENTAGE)).isInstanceOf(PercentageSplitCalculator.class);
        assertThat(factory.get(SplitType.SHARES)).isInstanceOf(SharesSplitCalculator.class);
    }

    @Test
    void factory_ShouldThrowOnNullType() {
        assertThatThrownBy(() -> factory.get(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("SplitType cannot be null");
    }

    @Test
    void equalSplit_ShouldDivideEquallyAndAllocatePennyRemainderToFirst() {
        SplitCalculator calc = factory.get(SplitType.EQUAL);
        BigDecimal total = new BigDecimal("10.00");
        Map<String, BigDecimal> participants = new LinkedHashMap<>();
        participants.put("userA", BigDecimal.ZERO);
        participants.put("userB", BigDecimal.ZERO);
        participants.put("userC", BigDecimal.ZERO);

        Map<String, BigDecimal> result = calc.calculate(total, participants, CurrencyCode.INR);

        // $10 / 3 = 3.33 each, leaving $0.01 remainder. Remainder added to userA.
        assertThat(result.get("userA")).isEqualTo(new BigDecimal("3.34"));
        assertThat(result.get("userB")).isEqualTo(new BigDecimal("3.33"));
        assertThat(result.get("userC")).isEqualTo(new BigDecimal("3.33"));

        // Sum must be exactly 10.00
        BigDecimal sum = result.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        assertThat(sum).isEqualTo(total);
    }

    @Test
    void exactSplit_ShouldValidateAndReturnExactAmounts() {
        SplitCalculator calc = factory.get(SplitType.EXACT);
        BigDecimal total = new BigDecimal("100.50");
        Map<String, BigDecimal> allocation = new LinkedHashMap<>();
        allocation.put("userA", new BigDecimal("40.25"));
        allocation.put("userB", new BigDecimal("60.25"));

        Map<String, BigDecimal> result = calc.calculate(total, allocation, CurrencyCode.INR);

        assertThat(result.get("userA")).isEqualTo(new BigDecimal("40.25"));
        assertThat(result.get("userB")).isEqualTo(new BigDecimal("60.25"));
    }

    @Test
    void exactSplit_ShouldThrowException_WhenSumDoesNotEqualTotal() {
        SplitCalculator calc = factory.get(SplitType.EXACT);
        BigDecimal total = new BigDecimal("100.00");
        Map<String, BigDecimal> allocation = new LinkedHashMap<>();
        allocation.put("userA", new BigDecimal("40.00"));
        allocation.put("userB", new BigDecimal("50.00"));

        assertThatThrownBy(() -> calc.calculate(total, allocation, CurrencyCode.INR))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not equal the total amount");
    }

    @Test
    void percentageSplit_ShouldDivideByPercentagesAndApplyPennyPolicy() {
        SplitCalculator calc = factory.get(SplitType.PERCENTAGE);
        BigDecimal total = new BigDecimal("10.00");
        Map<String, BigDecimal> allocation = new LinkedHashMap<>();
        allocation.put("userA", new BigDecimal("33.33"));
        allocation.put("userB", new BigDecimal("33.33"));
        allocation.put("userC", new BigDecimal("33.34"));

        Map<String, BigDecimal> result = calc.calculate(total, allocation, CurrencyCode.INR);

        // userA: 10 * 33.33% = 3.33
        // userB: 10 * 33.33% = 3.33
        // userC: 10 * 33.34% = 3.33
        // Sum = 9.99, penny remainder = 0.01 added to userA (first key) -> 3.34
        assertThat(result.get("userA")).isEqualTo(new BigDecimal("3.34"));
        assertThat(result.get("userB")).isEqualTo(new BigDecimal("3.33"));
        assertThat(result.get("userC")).isEqualTo(new BigDecimal("3.33"));
    }

    @Test
    void percentageSplit_ShouldThrowException_WhenSumPercentageNot100() {
        SplitCalculator calc = factory.get(SplitType.PERCENTAGE);
        BigDecimal total = new BigDecimal("100.00");
        Map<String, BigDecimal> allocation = new LinkedHashMap<>();
        allocation.put("userA", new BigDecimal("50.00"));
        allocation.put("userB", new BigDecimal("49.00"));

        assertThatThrownBy(() -> calc.calculate(total, allocation, CurrencyCode.INR))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must equal 100.00");
    }

    @Test
    void sharesSplit_ShouldDivideBySharesAndApplyPennyPolicy() {
        SplitCalculator calc = factory.get(SplitType.SHARES);
        BigDecimal total = new BigDecimal("10.00");
        Map<String, BigDecimal> allocation = new LinkedHashMap<>();
        allocation.put("userA", new BigDecimal("2")); // 2 shares
        allocation.put("userB", new BigDecimal("1")); // 1 share
        allocation.put("userC", new BigDecimal("1")); // 1 share
        // Total shares = 4. Shares value: A = 5.00, B = 2.50, C = 2.50. Sum = 10.00. No remainder.

        Map<String, BigDecimal> result = calc.calculate(total, allocation, CurrencyCode.INR);

        assertThat(result.get("userA")).isEqualTo(new BigDecimal("5.00"));
        assertThat(result.get("userB")).isEqualTo(new BigDecimal("2.50"));
        assertThat(result.get("userC")).isEqualTo(new BigDecimal("2.50"));
    }

    @Test
    void sharesSplit_ShouldThrowException_WhenSharesValueZeroOrLess() {
        SplitCalculator calc = factory.get(SplitType.SHARES);
        BigDecimal total = new BigDecimal("100.00");
        Map<String, BigDecimal> allocation = new LinkedHashMap<>();
        allocation.put("userA", new BigDecimal("1"));
        allocation.put("userB", new BigDecimal("0"));

        assertThatThrownBy(() -> calc.calculate(total, allocation, CurrencyCode.INR))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be greater than zero");
    }
}
