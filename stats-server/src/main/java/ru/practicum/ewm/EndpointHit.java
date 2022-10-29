package ru.practicum.ewm;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(name = "hits", schema = "public")
public class EndpointHit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @NonNull
    @Column(name = "app", nullable = false, length = 255)
    private String app;

    @NonNull
    @Column(name = "uri", nullable = false, length = 255)
    private String uri;

    @NonNull
    @Column(name = "ip", nullable = false, length = 255)
    private String ip;

    @NonNull
    @Column(name = "time_stamp", nullable = false)
    private LocalDateTime timeStamp;
}