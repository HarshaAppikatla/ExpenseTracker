package com.expenseflow.repository;

import com.expenseflow.BaseIntegrationTest;
import com.expenseflow.entity.RoleEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MigrationTest extends BaseIntegrationTest {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void testFlywayMigration_SeededRolesExist() {
        Optional<RoleEntity> userRole = roleRepository.findByName("ROLE_USER");
        Optional<RoleEntity> adminRole = roleRepository.findByName("ROLE_ADMIN");
        Optional<RoleEntity> superAdminRole = roleRepository.findByName("ROLE_SUPER_ADMIN");

        assertThat(userRole).isPresent();
        assertThat(userRole.get().getId()).isEqualTo("550e8400-e29b-41d4-a716-446655440000");

        assertThat(adminRole).isPresent();
        assertThat(adminRole.get().getId()).isEqualTo("550e8400-e29b-41d4-a716-446655440001");

        assertThat(superAdminRole).isPresent();
        assertThat(superAdminRole.get().getId()).isEqualTo("550e8400-e29b-41d4-a716-446655440002");
    }
}
