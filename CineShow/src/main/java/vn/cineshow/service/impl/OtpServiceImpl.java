package vn.cineshow.service.impl;

import com.sendgrid.SendGrid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import lombok.var;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.cineshow.model.OtpCode;
import vn.cineshow.repository.OtpCodeRepository;
import vn.cineshow.service.OtpService;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j(topic="OTP-SERVICE")
public class OtpServiceImpl implements OtpService {

    private final OtpCodeRepository otpRepo;
    private final EmailServiceImpl emailService;
    private final PasswordEncoder passwordEncoder; // tái dụng để hash OTP

    private final SendGrid sendgrid; // đã có bean trong SecurityConfig
    @Value("${spring.sendgrid.from-email}")
    private String fromEmail;

    private final String TEMPLATE_ID = "d-71a24bbc41824c0495bc166a115275a0";

    // policy
    private static final Duration EXPIRES_IN = Duration.ofMinutes(10);
    private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(60);
    private static final int MAX_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);
    private static final SecureRandom RNG = new SecureRandom();

    @Transactional
    @Override
    public void sendOtp(String email) {
        var now = Instant.now();
        var code = otpRepo.findByEmail(email).orElse(null);

        if (code != null) {
            if (code.getLockedUntil()!=null && code.getLockedUntil().isAfter(now)) {
                throw new IllegalStateException("Email is temporarily locked. Try later.");
            }
            if (code.getLastSentAt()!=null && code.getLastSentAt().plus(RESEND_COOLDOWN).isAfter(now)) {
                throw new IllegalStateException("Please wait before requesting another OTP.");
            }
        }

        String otp = generateOtp(6); // ví dụ 6 số
        String hash = passwordEncoder.encode(otp);

        if (code == null) {
            code = OtpCode.builder()
                    .email(email)
                    .otpHash(hash)
                    .expiresAt(now.plus(EXPIRES_IN))
                    .attempts(0)
                    .maxAttempts(MAX_ATTEMPTS)
                    .lastSentAt(now)
                    .used(false)
                    .build();
        } else {
            code.setOtpHash(hash);
            code.setExpiresAt(now.plus(EXPIRES_IN));
            code.setAttempts(0);
            code.setLastSentAt(now);
            code.setUsed(false);
            code.setLockedUntil(null);
        }
        otpRepo.save(code);

        // gửi email (nên async ở EmailService)
        emailService.send(email, "Your OTP code", "Your OTP is: " + otp);
    }

    @Transactional
    @Override
    public void verifyOtp(String email, String otp) {
        var now = Instant.now();
        var code = otpRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("OTP not found. Please request a new code."));

        if (code.getLockedUntil()!=null && code.getLockedUntil().isAfter(now)) {
            throw new IllegalStateException("Email is locked. Try later.");
        }
        if (code.isUsed()) {
            throw new IllegalStateException("OTP already used.");
        }
        if (code.getExpiresAt().isBefore(now)) {
            throw new IllegalStateException("OTP expired.");
        }
        // tăng attempts trước để ghi nhận
        code.setAttempts(code.getAttempts()+1);
        if (code.getAttempts() > code.getMaxAttempts()) {
            code.setLockedUntil(now.plus(LOCK_DURATION));
            otpRepo.save(code);
            throw new IllegalStateException("Too many attempts. Locked for a while.");
        }

        boolean ok = passwordEncoder.matches(otp, code.getOtpHash());
        if (!ok) {
            otpRepo.save(code);
            throw new IllegalArgumentException("Invalid OTP.");
        }

        code.setUsed(true);
        otpRepo.save(code);
    }

    @Override
    public boolean isEmailVerified(String email) {
        return otpRepo.findByEmail(email).map(OtpCode::isUsed).orElse(false);
    }

    @Transactional
    @Override
    public void clearState(String email) {
        otpRepo.deleteByEmail(email); // dọn sau khi đăng ký xong
    }

    private static String generateOtp(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i=0; i<len; i++) sb.append(RNG.nextInt(10));
        return sb.toString();
    }
}



