package ru.practicum.ewm.model.compilation;

import lombok.*;
import ru.practicum.ewm.model.event.Event;

import javax.persistence.*;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "compilations", schema = "public")
public class Compilation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NonNull
    @Column(name = "title", nullable = false)
    String title;

    @NonNull
    @Column(name = "pinned", nullable = false)
    private Boolean pinned;

    @NonNull
    @ManyToMany(cascade = {CascadeType.ALL})
    @JoinTable(name = "event_compilations",
            joinColumns = {@JoinColumn(name = "event_id")},
            inverseJoinColumns = {@JoinColumn(name = "compilation_id")}
    )
    private Set<Event> events;

}