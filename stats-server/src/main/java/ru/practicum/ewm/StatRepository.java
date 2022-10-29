package ru.practicum.ewm;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatRepository extends JpaRepository<EndpointHit, Integer> {

    public List<EndpointHit> findAllByUriAndTimeStampBetween(String uri, LocalDateTime start, LocalDateTime end);
}