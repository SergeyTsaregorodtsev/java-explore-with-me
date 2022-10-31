package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.model.request.ParticipationRequest;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<ParticipationRequest, Long> {

    List<ParticipationRequest> findAllByEvent_Id(long eventId);

    List<ParticipationRequest> findAllByRequesterId(long userId);

    ParticipationRequest findByEvent_IdAndRequester_Id(long eventId, long requesterId);

    @Query("select count(request) " +
           "from ParticipationRequest request " +
           "where request.event.id = ?1 and request.status = 'CONFIRMED'")
    int getConfirmedRequestsAmount(long eventId);

    List<ParticipationRequest> findAllByEvent_IdAndStatus(long eventId, ParticipationRequest.Status status);
}