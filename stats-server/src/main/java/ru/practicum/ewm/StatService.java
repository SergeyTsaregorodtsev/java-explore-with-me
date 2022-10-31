package ru.practicum.ewm;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatService {
    private final StatRepository repository;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void hit(EndpointHitDto dto) {
        EndpointHit hit = repository.save(EndpointHitMapper.toEndpointHit(dto));
        log.trace("Сохранён запрос ID {} на {} от пользователя IP {}.", hit.getId(), hit.getUri(), hit.getIp());
    }

    public List<ViewStats> get(String start, String end, String[] uris, boolean unique) {
        List<ViewStats> result = new ArrayList<>();
        for (String uri : uris) {
            List<EndpointHit> hits = repository.findAllByUriAndAppAndTimeStampBetween(
                    uri,
                    "ewm-main-service",
                    LocalDateTime.parse(start, formatter),
                    LocalDateTime.parse(end, formatter));
            int views;
            if (!unique) {
                views = hits.size();
            } else {
                Set<String> uniqueIPs = new HashSet<>();
                for (EndpointHit hit : hits) {
                    uniqueIPs.add(hit.getIp());
                }
                views = uniqueIPs.size();
            }
            result.add(new ViewStats("ewm-main-service", uri, views));
        }
        log.trace("Получены {} блока данных о запросах на {}.", result.size(), Arrays.toString(uris));
        return result;
    }
}