package com.expenseflow.util;

public final class MetricNames {
    public static final String LOGIN_SUCCESS = "auth.login.success";
    public static final String LOGIN_FAILURE = "auth.login.failure";
    public static final String ACCOUNT_LOCKED = "auth.account.locked";
    public static final String REFRESH_ROTATION = "auth.refresh.rotation";
    public static final String REFRESH_REPLAY = "auth.refresh.replay";
    public static final String RATE_LIMIT_EXCEEDED = "auth.rate.limit.exceeded";

    private MetricNames() {}
}
