package bitc.full502.spring.domain.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Entity @Table(name = "users")
public class Users {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "users_id", nullable = false, unique = true, length = 50)
    private String usersId;

    @Column(nullable = false, unique = true, length = 120)
    private String email;

    @Column(name = "pass", nullable = false, length = 255)
    private String pass;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 30)
    private String phone;
}
