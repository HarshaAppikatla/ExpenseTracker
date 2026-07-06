package com.expenseflow.core.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

@Configuration
@ConfigurationProperties(prefix = "expenseflow.storage")
@Validated
@Getter
@Setter
public class StorageProperties {

    @NotBlank(message = "Receipts upload directory must be configured")
    private String receiptsDir = "uploads/receipts";
}
