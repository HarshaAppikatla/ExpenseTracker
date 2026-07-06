package com.expenseflow.category.service;

import com.expenseflow.category.dto.CategoryRequest;
import com.expenseflow.category.dto.CategoryResponse;
import com.expenseflow.category.entity.CategoryEntity;
import com.expenseflow.category.mapper.CategoryMapper;
import com.expenseflow.category.repository.CategoryRepository;
import com.expenseflow.entity.UserEntity;
import com.expenseflow.exception.SecurityHardeningException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @Mock
    private com.expenseflow.repository.UserRepository userRepository;

    @InjectMocks
    private CategoryService categoryService;

    private UserEntity testUser;
    private CategoryRequest request;
    private CategoryEntity customEntity;
    private CategoryEntity systemEntity;
    private CategoryResponse customResponse;

    @BeforeEach
    void setUp() {
        testUser = new UserEntity();
        testUser.setId("user-uuid");
        testUser.setEmail("user@example.com");

        request = new CategoryRequest("Fun", "sports", "#FF5733");

        customEntity = CategoryEntity.builder()
                .id("cat-uuid")
                .user(testUser)
                .name("Fun")
                .icon("sports")
                .color("#FF5733")
                .systemCategory(false)
                .build();

        systemEntity = CategoryEntity.builder()
                .id("cat-sys")
                .user(null)
                .name("Food")
                .icon("restaurant")
                .color("#FF5733")
                .systemCategory(true)
                .build();

        customResponse = new CategoryResponse("cat-uuid", "Fun", "sports", "#FF5733", false);
    }

    @Test
    void testCreateCategory_Success() {
        when(userRepository.findById("user-uuid")).thenReturn(Optional.of(testUser));
        when(categoryRepository.findBySystemCategoryTrueAndNameIgnoreCase("Fun")).thenReturn(Optional.empty());
        when(categoryRepository.findByUserIdAndNameIgnoreCaseAndIsDeletedFalse("user-uuid", "Fun")).thenReturn(Optional.empty());
        when(categoryRepository.save(any(CategoryEntity.class))).thenReturn(customEntity);
        when(categoryMapper.toResponse(customEntity)).thenReturn(customResponse);

        CategoryResponse result = categoryService.createCategory(testUser.getId(), request);

        assertThat(result).isNotNull();
        assertThat(result.name()).isEqualTo("Fun");
        verify(categoryRepository).save(any(CategoryEntity.class));
    }

    @Test
    void testCreateCategory_DuplicateCustom_ThrowsException() {
        when(categoryRepository.findBySystemCategoryTrueAndNameIgnoreCase("Fun")).thenReturn(Optional.empty());
        when(categoryRepository.findByUserIdAndNameIgnoreCaseAndIsDeletedFalse("user-uuid", "Fun")).thenReturn(Optional.of(customEntity));

        assertThatThrownBy(() -> categoryService.createCategory(testUser.getId(), request))
                .isInstanceOf(SecurityHardeningException.class)
                .hasMessageContaining("You already have a category with this name")
                .extracting(e -> ((SecurityHardeningException) e).getCode())
                .isEqualTo("CAT_006");
    }

    @Test
    void testDeleteCategory_SystemCategory_ThrowsException() {
        when(categoryRepository.findById("cat-sys")).thenReturn(Optional.of(systemEntity));

        assertThatThrownBy(() -> categoryService.deleteCategory("user-uuid", "cat-sys", "user@example.com"))
                .isInstanceOf(SecurityHardeningException.class)
                .hasMessageContaining("System categories cannot be deleted")
                .extracting(e -> ((SecurityHardeningException) e).getCode())
                .isEqualTo("CAT_004");
    }
}
