package com.expenseflow.income.mapper;

import com.expenseflow.income.dto.IncomeResponse;
import com.expenseflow.income.entity.IncomeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IncomeMapper {

    @Mapping(target = "userId", source = "user.id")
    IncomeResponse toResponse(IncomeEntity entity);

    List<IncomeResponse> toResponseList(List<IncomeEntity> entities);
}
