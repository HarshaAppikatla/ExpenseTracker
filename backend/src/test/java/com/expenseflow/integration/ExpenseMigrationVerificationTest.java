package com.expenseflow.integration;

import com.expenseflow.BaseIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ExpenseMigrationVerificationTest extends BaseIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void verifySplitTypeColumnExistsAndDefaultsToEqual() {
        // Clean up any test records
        jdbcTemplate.execute("DELETE FROM expenses WHERE id = 'test-mig-1'");

        // Insert a row without specifying split_type to verify the DEFAULT constraint
        jdbcTemplate.execute(
            "INSERT INTO expenses (id, group_id, description, category, category_type, amount, currency, paid_by_user_id, created_by_user_id, status, expense_date, is_deleted) " +
            "VALUES ('test-mig-1', 'grp-1', 'Migration Test', 'FOOD', 'SYSTEM', 20.00, 'USD', 'user-1', 'user-1', 'DRAFT', '2026-07-06', false)"
        );

        // Retrieve split_type column value
        String splitType = jdbcTemplate.queryForObject(
            "SELECT split_type FROM expenses WHERE id = 'test-mig-1'", 
            String.class
        );

        assertThat(splitType).isEqualTo("EQUAL");

        // Clean up
        jdbcTemplate.execute("DELETE FROM expenses WHERE id = 'test-mig-1'");
    }
}
