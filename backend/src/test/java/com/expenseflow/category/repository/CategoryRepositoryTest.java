package com.expenseflow.category.repository;

import com.expenseflow.BaseIntegrationTest;
import com.expenseflow.category.entity.CategoryEntity;
import com.expenseflow.entity.UserEntity;
import com.expenseflow.entity.UserStatus;
import com.expenseflow.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class CategoryRepositoryTest extends BaseIntegrationTest {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testFindAllSystemAndUserCategories_ReturnsCombined() {
        UserEntity user = UserEntity.builder()
                .fullName("Category User")
                .email("category.user@example.com")
                .password("password")
                .status(UserStatus.ACTIVE)
                .loginProvider("LOCAL")
                .build();
        userRepository.save(user);

        CategoryEntity customCat = CategoryEntity.builder()
                .id("custom-uuid-1")
                .user(user)
                .name("Custom Category")
                .icon("sports")
                .color("#123456")
                .systemCategory(false)
                .build();
        categoryRepository.save(customCat);

        List<CategoryEntity> list = categoryRepository.findAllSystemAndUserCategories(user.getId());
        
        // Should return 10 default system categories + 1 custom category
        assertThat(list).hasSize(11);
        assertThat(list).anyMatch(c -> "Custom Category".equals(c.getName()));
        assertThat(list).anyMatch(c -> "Food".equals(c.getName()) && c.isSystemCategory());
    }
}
