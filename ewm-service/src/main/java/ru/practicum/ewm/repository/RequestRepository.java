package ru.practicum.ewm.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.ewm.model.request.ParticipationRequest;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<ParticipationRequest, Integer> {

    List<ParticipationRequest> findAllByEvent_Id(int eventId);

    List<ParticipationRequest> findAllByRequesterId(int userId);

    ParticipationRequest findByEvent_IdAndRequester_Id(int eventId, int requesterId);

    @Query("select count(request) " +
           "from ParticipationRequest request " +
           "where request.event.id = ?1 and request.status = 'CONFIRMED'")
    int getConfirmedRequestsAmount(int eventId);

    List<ParticipationRequest> findAllByEvent_IdAndAndStatus(int eventId, ParticipationRequest.Status status);
}