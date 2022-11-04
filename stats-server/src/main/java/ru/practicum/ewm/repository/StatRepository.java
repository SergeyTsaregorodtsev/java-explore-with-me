package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatRepository extends JpaRepository<EndpointHit, Long> {

    List<EndpointHit> findAllByUriAndApp_IdAndTimeStampBetween(String uri, Long appId, LocalDateTime start, LocalDateTime end);

    List<EndpointHit> findAllByApp_IdAndUriInAndTimeStampBetween
            (Long appId, List<String> uris, LocalDateTime start, LocalDateTime end);
}