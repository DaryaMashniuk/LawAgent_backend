package by.masnhyuk.lawAgent.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
public class Users {
    public Users(Long id, String username, String password, String email, String subscription, LocalDateTime createdAt, Boolean isActive) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.subscription = subscription;
        this.createdAt = createdAt;
        this.isActive = isActive;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String subscription;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "is_active")
    private Boolean isActive;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<FavouriteDocument> favouriteDocuments = new ArrayList<>();

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Users{");
        sb.append("id=").append(id);
        sb.append(", username='").append(username).append('\'');
        sb.append(", password='").append(password).append('\'');
        sb.append(", email='").append(email).append('\'');
        sb.append(", subscription='").append(subscription).append('\'');
        sb.append(", createdAt='").append(createdAt).append('\'');
        sb.append(", isActive='").append(isActive).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
