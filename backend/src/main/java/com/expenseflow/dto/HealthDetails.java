package com.expenseflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthDetails {
    private String name;
    private String version;
    private String environment;
    private String timestamp;
    private String status;
}
