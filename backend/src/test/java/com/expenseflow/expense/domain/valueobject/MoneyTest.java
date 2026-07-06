package com.expenseflow.expense.domain.valueobject;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoneyTest {

    @Test
    void moneyConstructor_ShouldSetScaleAndBeImmutable() {
        BigDecimal initialVal = new BigDecimal("100.5");
        Money m = new Money(initialVal, CurrencyCode.INR);

        assertThat(m.getAmount()).isEqualTo(new BigDecimal("100.50"));
        assertThat(m.getCurrency()).isEqualTo(CurrencyCode.INR);
    }

    @Test
    void moneyEqualsAndHashCode_ShouldBeValueEqual() {
        Money m1 = new Money(new BigDecimal("150"), CurrencyCode.USD);
        Money m2 = new Money(new BigDecimal("150.00"), CurrencyCode.USD);
        Money m3 = new Money(new BigDecimal("150"), CurrencyCode.EUR);

        assertThat(m1).isEqualTo(m2);
        assertThat(m1.hashCode()).isEqualTo(m2.hashCode());
        assertThat(m1).isNotEqualTo(m3);
    }

    @Test
    void addition_ShouldSucceed_WhenCurrenciesMatch() {
        Money m1 = new Money(new BigDecimal("20.5"), CurrencyCode.INR);
        Money m2 = new Money(new BigDecimal("30"), CurrencyCode.INR);

        Money result = m1.add(m2);

        assertThat(result.getAmount()).isEqualTo(new BigDecimal("50.50"));
        assertThat(result.getCurrency()).isEqualTo(CurrencyCode.INR);
        // Verify immutability: originals didn't change
        assertThat(m1.getAmount()).isEqualTo(new BigDecimal("20.50"));
    }

    @Test
    void addition_ShouldThrowException_WhenCurrenciesMismatch() {
        Money m1 = new Money(new BigDecimal("20"), CurrencyCode.INR);
        Money m2 = new Money(new BigDecimal("30"), CurrencyCode.USD);

        assertThatThrownBy(() -> m1.add(m2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Currency mismatch");
    }

    @Test
    void subtraction_ShouldSucceed_WhenCurrenciesMatch() {
        Money m1 = new Money(new BigDecimal("50"), CurrencyCode.INR);
        Money m2 = new Money(new BigDecimal("20.5"), CurrencyCode.INR);

        Money result = m1.subtract(m2);

        assertThat(result.getAmount()).isEqualTo(new BigDecimal("29.50"));
        assertThat(result.getCurrency()).isEqualTo(CurrencyCode.INR);
    }

    @Test
    void subtraction_ShouldThrowException_WhenCurrenciesMismatch() {
        Money m1 = new Money(new BigDecimal("50"), CurrencyCode.INR);
        Money m2 = new Money(new BigDecimal("20"), CurrencyCode.USD);

        assertThatThrownBy(() -> m1.subtract(m2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Currency mismatch");
    }
}
