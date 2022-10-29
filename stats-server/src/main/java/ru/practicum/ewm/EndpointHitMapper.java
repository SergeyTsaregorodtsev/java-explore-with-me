package ru.practicum.ewm;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class EndpointHitMapper {

    static EndpointHit toEndpointHit(EndpointHitDto dto) {
        LocalDateTime timeStamp = (dto.getTimeStamp() != null) ?
                LocalDateTime.parse(dto.getTimeStamp(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) :
                LocalDateTime.now();
        return new EndpointHit(dto.getApp(), dto.getUri(), dto.getIp(), timeStamp);
    }
}