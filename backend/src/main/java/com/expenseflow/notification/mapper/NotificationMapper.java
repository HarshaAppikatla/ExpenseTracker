package com.expenseflow.notification.mapper;

import com.expenseflow.notification.domain.entity.NotificationEntity;
import com.expenseflow.notification.domain.valueobject.NotificationPayload;
import com.expenseflow.notification.dto.NotificationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * MapStruct mapper for the Notification bounded context.
 * Flattens the {@link NotificationPayload} value object into the API response DTO.
 */
@Mapper(componentModel = "spring")
public interface NotificationMapper {

    @Mapping(source = "payload", target = "expenseId", qualifiedByName = "extractExpenseId")
    @Mapping(source = "payload", target = "groupId",   qualifiedByName = "extractGroupId")
    @Mapping(source = "payload", target = "tripId",    qualifiedByName = "extractTripId")
    NotificationResponse toResponse(NotificationEntity entity);

    @Named("extractExpenseId")
    default String extractExpenseId(NotificationPayload payload) {
        return payload != null ? payload.expenseId() : null;
    }

    @Named("extractGroupId")
    default String extractGroupId(NotificationPayload payload) {
        return payload != null ? payload.groupId() : null;
    }

    @Named("extractTripId")
    default String extractTripId(NotificationPayload payload) {
        return payload != null ? payload.tripId() : null;
    }
}
