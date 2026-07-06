package com.expenseflow.category.mapper;

import com.expenseflow.category.dto.CategoryResponse;
import com.expenseflow.category.entity.CategoryEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryResponse toResponse(CategoryEntity entity);

    List<CategoryResponse> toResponseList(List<CategoryEntity> entities);
}
