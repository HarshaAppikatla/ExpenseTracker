package com.expenseflow.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI expenseFlowOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ExpenseFlow REST API")
                        .description("REST API Documentation for the ExpenseFlow collaborative expense sharing system.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("ExpenseFlow Development Team")
                                .email("support@expenseflow.com")));
    }
}
