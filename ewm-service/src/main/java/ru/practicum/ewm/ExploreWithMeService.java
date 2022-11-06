package ru.practicum.ewm;

import com.google.gson.Gson;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ExploreWithMeService {

    public static void main(String[] args) {
        SpringApplication.run(ExploreWithMeService.class, args);
    }

    @Bean
    public Gson getGson() {
        return new Gson();
    }
}