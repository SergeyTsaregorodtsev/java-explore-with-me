package ru.practicum.ewm.model.user;

import lombok.*;

import javax.persistence.*;

@AllArgsConstructor
@RequiredArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "users", schema = "public")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name", nullable = false, length = 255)
    @NonNull
    private String name;

    @Column(name = "email", nullable = false, length = 255, unique = true)
    @NonNull
    private String email;
}