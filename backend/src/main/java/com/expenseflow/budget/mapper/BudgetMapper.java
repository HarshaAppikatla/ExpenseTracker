package com.expenseflow.budget.mapper;

import com.expenseflow.budget.dto.BudgetDto;
import com.expenseflow.budget.entity.BudgetEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BudgetMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    BudgetDto toDto(BudgetEntity entity);
}
