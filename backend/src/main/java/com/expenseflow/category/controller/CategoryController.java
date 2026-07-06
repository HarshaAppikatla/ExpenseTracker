package com.expenseflow.category.controller;

import com.expenseflow.category.dto.CategoryRequest;
import com.expenseflow.category.dto.CategoryResponse;
import com.expenseflow.category.service.CategoryService;
import com.expenseflow.dto.ApiResponse;
import com.expenseflow.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories(@AuthenticationPrincipal UserPrincipal principal) {
        List<CategoryResponse> categories = categoryService.getCategories(principal.getId());
        return ResponseEntity.ok(ApiResponse.success("Categories retrieved successfully", categories));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CategoryRequest request) {

        CategoryResponse category = categoryService.createCategory(principal.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Category created successfully", category));
    }


    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id,
            @Valid @RequestBody CategoryRequest request) {

        CategoryResponse category = categoryService.updateCategory(principal.getId(), id, request);
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully", category));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable("id") String id) {

        categoryService.deleteCategory(principal.getId(), id, principal.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Category deleted successfully"));
    }
}
