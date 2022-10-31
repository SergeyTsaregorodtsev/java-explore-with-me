package ru.practicum.ewm.model.event;

import lombok.*;
import ru.practicum.ewm.model.category.Category;
import ru.practicum.ewm.model.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@RequiredArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "events", schema = "public")
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NonNull
    @ManyToOne @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @NonNull
    @Column(name = "title", nullable = false, length = 120)
    private String title;

    @NonNull
    @Column(name = "annotation", nullable = false, length = 2000)
    private String annotation;

    @NonNull
    @Column(name = "description", length = 7000)
    private String description;

    @NonNull
    @ManyToOne @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;

    @NonNull
    @Column(name = "event_date", nullable = false)
    private LocalDateTime eventDate;

    @NonNull
    @Column(name = "created_on")
    private LocalDateTime createdOn;

    @Column(name = "published_on")
    private LocalDateTime publishedOn;

    @NonNull
    @Column(name = "location_lat", nullable = false)
    private float locationLat;

    @NonNull
    @Column(name = "location_lon", nullable = false)
    private float locationLon;

    @NonNull
    @Column(name = "paid", nullable = false)
    private boolean paid;

    @NonNull
    @Column(name = "request_moderation")
    private boolean requestModeration;

    @NonNull
    @Column(name = "participant_limit")
    private int participantLimit;

    @NonNull
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private State state;

    @Column(name = "confirmed_requests")
    private int confirmedRequests;

    public enum State {
        PENDING, PUBLISHED, CANCELED
    }
}