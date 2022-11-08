package ru.practicum.ewm.model.comment;

import lombok.*;
import org.hibernate.validator.constraints.Length;
import ru.practicum.ewm.model.event.Event;
import ru.practicum.ewm.model.user.User;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "comments", schema = "public")
public final class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NonNull
    @ManyToOne @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @NonNull
    @ManyToOne @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @NonNull
    @Column(name = "created", nullable = false)
    private LocalDateTime created;

    @NonNull
    @Length(min = 3, max = 2000)
    @Column(name = "text", nullable = false)
    private String text;

    @NonNull
    @Column(name = "estimation", nullable = false)
    @Enumerated(EnumType.STRING)
    private Estimation estimation;

    public enum Estimation {
        NEGATIVE, NEUTRAL, POSITIVE
    }
}