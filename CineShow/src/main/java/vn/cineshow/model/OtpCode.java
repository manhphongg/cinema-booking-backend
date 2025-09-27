
package vn.cineshow.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name="otp_codes", indexes = {
        @Index(name="idx_otp_email", columnList="email", unique=true)
})
@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class OtpCode {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true)
    private String email;

    @Column(nullable=false)
    private String otpHash;

    @Column(nullable=false)
    private Instant expiresAt;

    @Column(nullable=false)
    private boolean used;        // đã verify xong -> true
}
