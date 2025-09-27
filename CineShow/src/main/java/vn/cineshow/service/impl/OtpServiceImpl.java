package vn.cineshow.service.impl;

import com.sendgrid.SendGrid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.cineshow.exception.DuplicateResourceException;
import vn.cineshow.exception.ResourceNotFoundException;
import vn.cineshow.model.OtpCode;
import vn.cineshow.repository.OtpCodeRepository;
import vn.cineshow.service.OtpService;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "OTP-SERVICE")
public class OtpServiceImpl implements OtpService {

    private final OtpCodeRepository otpRepo;
    private final EmailServiceImpl emailService;
    private final PasswordEncoder passwordEncoder;
    private final SendGrid sendgrid;

    @Value("${spring.sendgrid.from-email}")
    private String fromEmail;

    private final String TEMPLATE_ID = "d-71a24bbc41824c0495bc166a115275a0";

    // =====  POLICY =====
    private static final Duration EXPIRES_IN = Duration.ofMinutes(4);
    private static final SecureRandom RNG = new SecureRandom();

    @Transactional
    @Override
    public void sendOtp(String email, String name) {
        var now = Instant.now();
        var code = otpRepo.findByEmail(email).orElse(null);

        String otp = generateOtp(6);
        String hash = passwordEncoder.encode(otp);

        if (code == null) {
            code = OtpCode.builder()
                    .email(email)
                    .otpHash(hash)
                    .expiresAt(now.plus(EXPIRES_IN))
                    .used(false)
                    .build();
        } else {
            code.setOtpHash(hash);
            code.setExpiresAt(now.plus(EXPIRES_IN));
            code.setUsed(false);
        }
        otpRepo.save(code);

        // gửi email (nên async nếu load cao)
//        emailService.sendOtpEmail(email, name, otp);
        emailService.send(email, "Your OTP code", "Hello " +name+". Your OTP is: " + otp);
    }

    @Transactional
    @Override
    public void verifyOtp(String email, String otp) {
        Instant now = Instant.now();
        var code = otpRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("OTP not found. Please request a new code."));

        if (code.isUsed()) {
            throw new DuplicateResourceException("OTP already used.");
        }
        if (code.getExpiresAt().isBefore(now)) {
            throw new IllegalStateException("OTP expired.");
        }

        boolean ok = passwordEncoder.matches(otp, code.getOtpHash());
        if (!ok) {
            throw new IllegalArgumentException("Invalid OTP.");
        }

        code.setUsed(true);
        otpRepo.save(code);
    }

    @Override
    public boolean isEmailVerified(String email) {
        return otpRepo.findByEmail(email)
                .map(OtpCode::isUsed)
                .orElse(false);
    }

    @Transactional
    @Override
    public void clearState(String email) {
        otpRepo.deleteByEmail(email);
    }

    private static String generateOtp(int len) {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) sb.append(RNG.nextInt(10));
        return sb.toString();
    }
}
