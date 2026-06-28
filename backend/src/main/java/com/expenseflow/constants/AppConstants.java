package com.expenseflow.constants;

public final class AppConstants {
    private AppConstants() {
        // Prevent instantiation
    }

    public static final String API_VERSION = "v1";
    public static final String API_BASE_PATH = "/api/" + API_VERSION;
    
    public static final String HEALTH_OK = "UP";
    public static final String HEALTH_DOWN = "DOWN";
}
