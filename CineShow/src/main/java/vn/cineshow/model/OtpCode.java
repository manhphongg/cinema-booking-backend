
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
    private int attempts;        // số lần nhập đã dùng

    @Column(nullable=false)
    private int maxAttempts;     // vd 5

    @Column(nullable=false)
    private Instant lastSentAt;  // phục vụ cooldown resend

    @Column
    private Instant lockedUntil; // khóa tạm sau khi quá số lần/sai nhiều

    @Column(nullable=false)
    private boolean used;        // đã verify xong -> true
}
