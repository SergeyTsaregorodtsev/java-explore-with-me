package ru.practicum.ewm.statclient;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StatClient {
    String url;
    String appName;
    Gson gson;
    static HttpClient client = HttpClient.newHttpClient();
    static HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    static Charset charset = java.nio.charset.StandardCharsets.UTF_8;

    @Autowired
    public StatClient(@Value("${statServerUrl:http://localhost:9090}") String url,
                      @Value("${appName}") String appName,
                      Gson gson) {
        this.url = url;
        this.appName = appName;
        this.gson = gson;
    }

    public void sendStat(EndpointHitDto hit) {
        String json = gson.toJson(hit);
        HttpRequest.BodyPublisher body = HttpRequest.BodyPublishers.ofString(json);
        HttpRequest request = HttpRequest.newBuilder()
                .POST(body)
                .uri(URI.create(url + "/hit"))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "text.html")
                .header("Content-Type","application/json")
                .build();
        try {
            HttpResponse<String> response = client.send(request, handler);
            if (response.statusCode() == 200) {
                log.trace("Отправлен POST-запрос в Stats по {} от пользователя IP {}.", hit.getUri(), hit.getIp());
            }
        } catch (IOException | InterruptedException e) {
            log.trace("Ошибка: POST-запрос в Stats не отправлен");
        }
    }

    public List<ViewStats> receiveStat(LocalDateTime start, LocalDateTime end, String[] uris, boolean unique) {
        StringBuilder sb = new StringBuilder();
        sb.append("?start=");
        sb.append(URLEncoder.encode(start.format(formatter),charset));
        sb.append("&end=");
        sb.append(URLEncoder.encode(end.format(formatter),charset));
        for (String uri : uris) {
            sb.append("&uris=");
            sb.append(URLEncoder.encode(uri,charset));
        }
        sb.append("&unique=");
        sb.append(unique);
        sb.append("&app=");
        sb.append(appName);

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url + "/stats" + sb))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();
        log.trace("Отправлен GET-запрос в Stats по событиям: {}, URI: {}", Arrays.toString(uris), request.uri());
        String result = "";
        try {
            HttpResponse<String> response = client.send(request, handler);
            if (response.statusCode() == 200) {
                log.trace("Получен ответ на GET-запрос в Stats.");
                result = response.body();
            } else {
                throw new IOException();
            }
        } catch (IOException | InterruptedException e) {
            log.trace("Ошибка: не получен ответ на GET-запрос в Stats.");
            throw new RuntimeException("Ошибка: не получен ответ на GET-запрос в Stats.");
        }

        List<ViewStats> stats = new ArrayList<>();
        JsonElement element = JsonParser.parseString(result);
        JsonArray array = element.getAsJsonArray();
        for (JsonElement jsonElement : array) {
            ViewStats viewStats = gson.fromJson(jsonElement.toString(), ViewStats.class);
            stats.add(viewStats);
            log.trace("Событие {} - просмотров {}.", viewStats.getUri(), viewStats.getHits());
        }
        return stats;
    }

    public int getViews(long eventId) {
        String[] uri = new String[]{"/events/" + eventId};
        List<ViewStats> stats = receiveStat(LocalDateTime.now().minusHours(1), LocalDateTime.now(), uri, true);
        return stats.get(0).getHits();
    }

    public Map<Long, Integer> getViews(List<Long> eventIds) {
        List<ViewStats> views = receiveStat(
                LocalDateTime.now().minusHours(1),
                LocalDateTime.now(),
                eventIds
                        .stream()
                        .map((Long s) -> "/events/" + s)
                        .toArray(String[]::new),
                true);
        Map<Long, Integer> viewsMap = new HashMap<>();
        for (int i = 0; i < eventIds.size(); i++) {
            viewsMap.put(eventIds.get(i), views.get(i).getHits());
        }
        return viewsMap;
    }
}