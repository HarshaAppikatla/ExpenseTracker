package com.expenseflow.profile.event;

import com.expenseflow.entity.UserEntity;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

@Getter
public class UserProfileOnboardedEvent extends ApplicationEvent {
    private final UserEntity user;
    private final String preferredCurrency;
    private final BigDecimal openingBalance;
    private final BigDecimal initialMonthlyIncome;

    public UserProfileOnboardedEvent(Object source, UserEntity user, String preferredCurrency, BigDecimal openingBalance, BigDecimal initialMonthlyIncome) {
        super(source);
        this.user = user;
        this.preferredCurrency = preferredCurrency;
        this.openingBalance = openingBalance;
        this.initialMonthlyIncome = initialMonthlyIncome;
    }
}
