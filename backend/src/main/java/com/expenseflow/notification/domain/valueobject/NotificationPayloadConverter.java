package com.expenseflow.notification.domain.valueobject;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

@Converter(autoApply = true)
@Slf4j
public class NotificationPayloadConverter implements AttributeConverter<NotificationPayload, String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(NotificationPayload attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize NotificationPayload to JSON", e);
            throw new IllegalArgumentException("Error serializing notification payload", e);
        }
    }

    @Override
    public NotificationPayload convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.readValue(dbData, NotificationPayload.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize JSON to NotificationPayload", e);
            throw new IllegalArgumentException("Error deserializing notification payload", e);
        }
    }
}
