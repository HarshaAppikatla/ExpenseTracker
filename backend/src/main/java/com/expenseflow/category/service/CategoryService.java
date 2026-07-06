package com.expenseflow.category.service;

import com.expenseflow.category.dto.CategoryRequest;
import com.expenseflow.category.dto.CategoryResponse;
import com.expenseflow.category.entity.CategoryEntity;
import com.expenseflow.category.mapper.CategoryMapper;
import com.expenseflow.category.repository.CategoryRepository;
import com.expenseflow.entity.UserEntity;
import com.expenseflow.exception.SecurityHardeningException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final com.expenseflow.repository.UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategories(String userId) {
        List<CategoryEntity> categories = categoryRepository.findAllSystemAndUserCategories(userId);
        return categoryMapper.toResponseList(categories);
    }

    public CategoryResponse createCategory(String userId, CategoryRequest request) {
        validateCategoryUniqueness(userId, request.name());

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        CategoryEntity category = CategoryEntity.builder()
                .id(UUID.randomUUID().toString())
                .user(user)
                .name(request.name())
                .icon(request.icon())
                .color(request.color())
                .systemCategory(false)
                .build();

        CategoryEntity saved = categoryRepository.save(category);
        log.info("Custom category created successfully: {} for user {}", request.name(), user.getEmail());
        return categoryMapper.toResponse(saved);
    }


    public CategoryResponse updateCategory(String userId, String categoryId, CategoryRequest request) {
        CategoryEntity category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new SecurityHardeningException("Category not found.", "CAT_001"));

        if (category.isSystemCategory()) {
            throw new SecurityHardeningException("System categories cannot be modified.", "CAT_002");
        }

        if (!category.getUser().getId().equals(userId)) {
            throw new SecurityHardeningException("You do not own this category.", "CAT_003");
        }

        if (!category.getName().equalsIgnoreCase(request.name())) {
            validateCategoryUniqueness(userId, request.name());
        }

        category.setName(request.name());
        category.setIcon(request.icon());
        category.setColor(request.color());

        CategoryEntity saved = categoryRepository.save(category);
        log.info("Category updated successfully: {}", categoryId);
        return categoryMapper.toResponse(saved);
    }

    public void deleteCategory(String userId, String categoryId, String email) {
        CategoryEntity category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new SecurityHardeningException("Category not found.", "CAT_001"));

        if (category.isSystemCategory()) {
            throw new SecurityHardeningException("System categories cannot be deleted.", "CAT_004");
        }

        if (!category.getUser().getId().equals(userId)) {
            throw new SecurityHardeningException("You do not own this category.", "CAT_003");
        }

        // Soft Delete
        category.setDeleted(true);
        category.setDeletedAt(LocalDateTime.now());
        category.setDeletedBy(email);

        categoryRepository.save(category);
        log.info("Category soft deleted successfully: {}", categoryId);
    }

    private void validateCategoryUniqueness(String userId, String name) {
        if (categoryRepository.findBySystemCategoryTrueAndNameIgnoreCase(name).isPresent()) {
            throw new SecurityHardeningException("A system category with this name already exists.", "CAT_005");
        }
        if (categoryRepository.findByUserIdAndNameIgnoreCaseAndIsDeletedFalse(userId, name).isPresent()) {
            throw new SecurityHardeningException("You already have a category with this name.", "CAT_006");
        }
    }
}
