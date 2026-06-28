package com.expenseflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InfoDetails {
    private String application;
    private String version;
    private String environment;
    private String javaVersion;
    private String springBootVersion;
    private String buildTime;
    private String uptime;
}
