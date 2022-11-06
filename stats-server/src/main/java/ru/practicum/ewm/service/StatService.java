package ru.practicum.ewm.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.ewm.model.*;
import ru.practicum.ewm.repository.AppRepository;
import ru.practicum.ewm.repository.StatRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatService {
    private final StatRepository statRepository;
    private final AppRepository appRepository;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void hit(EndpointHitDto dto) {
        App app = appRepository.findByName(dto.getApp());
        EndpointHit hit = statRepository.save(EndpointHitMapper.toEndpointHit(dto,app));
        log.trace("Сохранён запрос ID {} на {} от пользователя IP {} от приложения {} (ID {}).",
                hit.getId(),
                hit.getUri(),
                hit.getIp(),
                app.getName(),
                app.getId());
    }

    public List<ViewStats> get(String start, String end, String[] uris, boolean unique, String appName) {
        App app = appRepository.findByName(appName);
        List<ViewStats> result = new ArrayList<>();
        List<EndpointHit> hits = statRepository.findAllByApp_IdAndUriInAndTimeStampBetween(
                app.getId(),
                Arrays.asList(uris),
                LocalDateTime.parse(start, formatter),
                LocalDateTime.parse(end, formatter));
        for (String uri : uris) {
            long views = (unique) ?
                    hits.stream().filter(x -> x.getUri().equals(uri)).distinct().count() :
                    hits.stream().filter(x -> x.getUri().equals(uri)).count();
            result.add(new ViewStats(app.getName(), uri, (int)views));
        }
        log.trace("Получены {} блока данных о запросах на {} от приложения {} (ID {}).",
                result.size(),
                Arrays.toString(uris),
                app.getName(),
                app.getId());
        return result;
    }
}