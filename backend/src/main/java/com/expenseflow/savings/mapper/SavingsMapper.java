package com.expenseflow.savings.mapper;

import com.expenseflow.savings.dto.SavingsDepositDto;
import com.expenseflow.savings.dto.SavingsGoalDto;
import com.expenseflow.savings.entity.SavingsDepositEntity;
import com.expenseflow.savings.entity.SavingsGoalEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SavingsMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "currentAmount", ignore = true)
    @Mapping(target = "progressPercentage", ignore = true)
    SavingsGoalDto toGoalDto(SavingsGoalEntity entity);

    @Mapping(target = "goalId", source = "savingsGoal.id")
    SavingsDepositDto toDepositDto(SavingsDepositEntity entity);
}
