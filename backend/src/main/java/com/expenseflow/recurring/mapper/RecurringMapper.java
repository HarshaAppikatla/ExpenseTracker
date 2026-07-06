package com.expenseflow.recurring.mapper;

import com.expenseflow.recurring.dto.RecurringDto;
import com.expenseflow.recurring.entity.RecurringTransactionEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RecurringMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    @Mapping(target = "status", expression = "java(entity.getStatus().name())")
    RecurringDto toDto(RecurringTransactionEntity entity);
}
