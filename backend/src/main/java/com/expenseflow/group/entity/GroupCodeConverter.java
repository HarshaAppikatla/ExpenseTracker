package com.expenseflow.group.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class GroupCodeConverter implements AttributeConverter<GroupCode, String> {
    @Override
    public String convertToDatabaseColumn(GroupCode attribute) {
        return attribute != null ? attribute.value() : null;
    }

    @Override
    public GroupCode convertToEntityAttribute(String dbData) {
        return dbData != null ? new GroupCode(dbData) : null;
    }
}
