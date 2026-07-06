package com.expenseflow.settlement.mapper;

import com.expenseflow.settlement.domain.entity.SettlementEntity;
import com.expenseflow.settlement.dto.SettlementResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SettlementMapper {

    @Mapping(target = "amount", source = "money.amount")
    @Mapping(target = "currency", source = "money.currency")
    @Mapping(target = "fromUserName", ignore = true)
    @Mapping(target = "toUserName", ignore = true)
    SettlementResponse toResponse(SettlementEntity entity);
}
