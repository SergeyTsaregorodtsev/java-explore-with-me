package ru.practicum.ewm.model;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EndpointHitMapper {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static EndpointHit toEndpointHit(EndpointHitDto dto, App app) {
        LocalDateTime timeStamp = (dto.getTimeStamp() != null) ?
                LocalDateTime.parse(dto.getTimeStamp(), formatter) :
                LocalDateTime.now();
        return new EndpointHit(app, dto.getUri(), dto.getIp(), timeStamp);
    }
}