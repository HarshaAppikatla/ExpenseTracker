package com.expenseflow.trip.mapper;

import com.expenseflow.trip.domain.entity.TripActivityEntity;
import com.expenseflow.trip.domain.entity.TripEntity;
import com.expenseflow.trip.domain.entity.TripParticipantEntity;
import com.expenseflow.trip.domain.valueobject.Destination;
import com.expenseflow.trip.domain.valueobject.TripSchedule;
import com.expenseflow.trip.domain.valueobject.TripSettings;
import com.expenseflow.trip.dto.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TripMapper {

    @Mapping(target = "organizerName", ignore = true)
    @Mapping(target = "status", expression = "java(entity.getStatus().name())")
    @Mapping(target = "participants", source = "entity.participants")
    TripDto toDto(TripEntity entity);

    DestinationDto toDestinationDto(Destination dest);
    Destination toDestination(DestinationDto dto);

    TripScheduleDto toScheduleDto(TripSchedule schedule);
    TripSchedule toSchedule(TripScheduleDto dto);

    TripSettingsDto toSettingsDto(TripSettings settings);
    TripSettings toSettings(TripSettingsDto dto);

    @Mapping(target = "userName", ignore = true)
    @Mapping(target = "userEmail", ignore = true)
    @Mapping(target = "status", expression = "java(entity.getStatus().name())")
    TripParticipantDto toParticipantDto(TripParticipantEntity entity);

    @Mapping(target = "tripId", source = "entity.trip.id")
    @Mapping(target = "actorName", ignore = true)
    @Mapping(target = "targetName", ignore = true)
    TripActivityDto toActivityDto(TripActivityEntity entity);
}
