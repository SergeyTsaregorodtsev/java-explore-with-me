package ru.practicum.ewm.model;

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
    private long id;

    @NonNull
    @ManyToOne @JoinColumn(name = "app_id", nullable = false)
    private App app;

    @NonNull
    @Column(name = "uri", nullable = false, length = 255)
    private String uri;

    @NonNull
    @Column(name = "ip", nullable = false, length = 255)
    private String ip;

    @NonNull
    @Column(name = "time_stamp", nullable = false)
    private LocalDateTime timeStamp;

    @Override
    public boolean equals(Object obj) {
        EndpointHit endpointHit = (EndpointHit) obj;
        if (obj.getClass() != EndpointHit.class) {
            return false;
        }
        return ip.equals(endpointHit.getIp());
    }
}