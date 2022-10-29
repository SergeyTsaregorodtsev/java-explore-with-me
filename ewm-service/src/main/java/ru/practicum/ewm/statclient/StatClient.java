package ru.practicum.ewm.statclient;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class StatClient {
    static String url = "http://localhost:9090";
    static Gson gson = new Gson();
    static HttpClient client = HttpClient.newHttpClient();
    static HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void sendStat(EndpointHitDto hit) {
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

    public static List<ViewStats> receiveStat(LocalDateTime start, LocalDateTime end, String[] uris, boolean unique) {
        StringBuilder sb = new StringBuilder();
        sb.append("?start=");
        sb.append(URLEncoder.encode(start.format(formatter),java.nio.charset.StandardCharsets.UTF_8));
        sb.append("&end=");
        sb.append(URLEncoder.encode(end.format(formatter),java.nio.charset.StandardCharsets.UTF_8));
        for (String uri : uris) {
            sb.append("&uris=");
            sb.append(URLEncoder.encode(uri,java.nio.charset.StandardCharsets.UTF_8));
        }
        sb.append("&unique=");
        sb.append(unique);

        HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(url + "/stats" + sb))
                .version(HttpClient.Version.HTTP_1_1)
                .header("Accept", "application/json")
                .build();
        log.trace("Отправлен GET-запрос в Stats по событиям: {}", Arrays.toString(uris));
        String result = "";
        try {
            HttpResponse<String> response = client.send(request, handler);
            if (response.statusCode() == 200) {
                log.trace("Получен ответ на GET-запрос в Stats.");
                result = response.body();
            } else {
                log.trace("Ошибка: не получен ответ на GET-запрос в Stats.");
            }
        } catch (IOException | InterruptedException e) {
            System.out.println(e.getMessage());
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

    public static int getViews(int eventId) {
        String[] uri = new String[]{"/events/" + eventId};
        List<ViewStats> stats = receiveStat(LocalDateTime.now().minusHours(1), LocalDateTime.now(), uri, true);
        return stats.get(0).getHits();
    }
}