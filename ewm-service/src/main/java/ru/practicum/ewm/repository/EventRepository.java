package ru.practicum.ewm.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.model.event.Event;

import java.util.List;


@Repository
public interface EventRepository extends JpaRepository<Event, Long>, QuerydslPredicateExecutor<Event> {

    Page<Event> findAllByInitiator_Id(long initiatorId, Pageable page);

    List<Event> findAllByIdIn(List<Long> id);

    @Query("select count(event) " +
            "from Event event " +
            "where event.category.id = ?1")
    int getCategoryEventAmount(long eventId);
}