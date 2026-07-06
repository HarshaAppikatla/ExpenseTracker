package com.expenseflow;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class BaseIntegrationTest {

    @Container
    protected static final MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.4.0")
            .withDatabaseName("expenseflow_test")
            .withUsername("testuser")
            .withPassword("testpassword");

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate"); // Flyway will manage schema
        registry.add("spring.flyway.enabled", () -> "true");
    }
}
