package vn.cineshow.model;

import jakarta.persistence.*;
import lombok.*;
import vn.cineshow.enums.AuthProvider;

import java.io.Serializable;

@Entity
@Table(
        name = "account_provider",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"account_id", "provider"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountProvider extends AbstractEntity implements Serializable {


    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthProvider provider; // LOCAL, GOOGLE, ...

    private String providerId; // sub from Google, null when LOCAL

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

}
