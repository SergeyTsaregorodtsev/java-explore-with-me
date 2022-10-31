package ru.practicum.ewm;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatRepository extends JpaRepository<EndpointHit, Long> {

    List<EndpointHit> findAllByUriAndAppAndTimeStampBetween(String uri, String app, LocalDateTime start, LocalDateTime end);
}