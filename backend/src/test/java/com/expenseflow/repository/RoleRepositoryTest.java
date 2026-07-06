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
class RoleRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void testFindByName_ReturnsSeededRole() {
        Optional<RoleEntity> userRole = roleRepository.findByName("ROLE_USER");
        assertThat(userRole).isPresent();
        assertThat(userRole.get().getName()).isEqualTo("ROLE_USER");
    }

    @Test
    void testSaveRole_Success() {
        RoleEntity customRole = RoleEntity.builder()
                .name("ROLE_CUSTOM")
                .build();

        roleRepository.save(customRole);

        Optional<RoleEntity> found = roleRepository.findByName("ROLE_CUSTOM");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("ROLE_CUSTOM");
    }
}
