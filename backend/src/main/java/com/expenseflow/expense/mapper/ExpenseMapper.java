package com.expenseflow.expense.mapper;

import com.expenseflow.expense.domain.entity.*;
import com.expenseflow.expense.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ExpenseMapper {

    @Mapping(target = "amount", source = "entity.money.amount")
    @Mapping(target = "currency", expression = "java(entity.getMoney().getCurrency().name())")
    @Mapping(target = "paidByUserName", ignore = true)
    @Mapping(target = "createdByUserName", ignore = true)
    ExpenseDto toDto(ExpenseEntity entity);

    @Mapping(target = "userName", ignore = true)
    @Mapping(target = "userEmail", ignore = true)
    ExpenseParticipantDto toParticipantDto(ExpenseParticipantEntity entity);

    @Mapping(target = "userName", ignore = true)
    @Mapping(target = "userEmail", ignore = true)
    ExpenseSplitDto toSplitDto(ExpenseSplitEntity entity);

    ExpenseAttachmentDto toAttachmentDto(ExpenseAttachmentEntity entity);
}
