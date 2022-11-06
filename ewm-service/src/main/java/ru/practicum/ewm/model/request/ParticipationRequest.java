package ru.practicum.ewm.model.request;

import lombok.*;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@RequiredArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "requests", schema = "public")
public class ParticipationRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NonNull
    @ManyToOne @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @NonNull
    @ManyToOne @JoinColumn(name = "requester_id", nullable = false)
    private User requester;

    @NonNull
    @Column(name = "created", nullable = false)
    private LocalDateTime created;

    @NonNull
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status;

    public enum Status {
        PENDING, CONFIRMED, REJECTED, CANCELED
    }
}