package vn.cineshow.service.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import vn.cineshow.dto.request.EmailRegisterRequest;
import vn.cineshow.dto.request.ForgotPasswordRequest;
import vn.cineshow.dto.request.ResetPasswordRequest;
import vn.cineshow.enums.AccountStatus;
import vn.cineshow.enums.AuthProvider;
import vn.cineshow.model.*;
import vn.cineshow.repository.AccountRepository;
import vn.cineshow.repository.OtpCodeRepository;
import vn.cineshow.repository.PasswordResetTokenRepository;
import vn.cineshow.repository.RefreshTokenRepository;
import vn.cineshow.service.AccountService;
import vn.cineshow.service.OtpService;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "OTP-SERVICE")
class AccountServiceImpl implements AccountService {


    private static final SecureRandom RNG = new SecureRandom();

    private final AccountRepository accountRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository; // keep if you have it
    private final OtpService otpService; // delegate all OTP logic here

    private static String base64Url(byte[] b) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(b);
    }

    @Override
    public long createCustomerAccount(EmailRegisterRequest req) {
        User user = User.builder()
                .name(req.name())
                .gender(req.gender())
                .dateOfBirth(req.dateOfBirth()).build();

        Account account = new Account();
        Account.builder()
                .email(req.email())
                .password(req.password())
                .status(AccountStatus.PENDING)
                .user(user)
                .build();

        AccountProvider provider = AccountProvider.builder()
                .account(account)
                .provider(AuthProvider.LOCAL)
                .build();
        account.setProviders(List.of(provider));
        accountRepository.save(account);
        return account.getId();
    }

    // -----------------------------------------------------------------------------------
    // Forgot password: neutral response (do not disclose email existence)
    // -----------------------------------------------------------------------------------
    @Override
    @Transactional
    public boolean forgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail();

        // Keep neutral if account not found
        var accOpt = accountRepository.findByEmail(email);
        if (!accOpt.isPresent()) {
            return true;
        }

        String name = "there";
        try {
            Object obj = accOpt.get();
            if (obj instanceof Account acc) {
                if (acc.getUser() != null && acc.getUser().getName() != null && !acc.getUser().getName().isBlank()) {
                    name = acc.getUser().getName();
                }
            }
        } catch (Exception ignore) {
            // keep neutral name
        }

        try {
            otpService.sendOtp(email, name);
            return true;
        } catch (Exception ex) {
            // Do not leak details to client; keep neutral success
            log.error("Send OTP failed for {}", email, ex);
            return true;
        }
    }

    // -----------------------------------------------------------------------------------
    // Verify OTP and issue reset token (returned to controller)
    // -----------------------------------------------------------------------------------
    @Override
    @Transactional
    public Optional<String> verifyOtpForReset(String email, String otpInput) {
        // STEP 1: Verify the plain OTP against the hashed value in DB (via OtpService).
        boolean verified;
        try {
            verified = otpService.verifyOtp(email, otpInput);
        } catch (Exception ex) {
            verified = false;
        }
        if (!verified) {
            return Optional.empty();
        }

        // STEP 2: Generate a fresh verifier (random, URL-safe) and hash it.
        byte[] random = new byte[48];
        RNG.nextBytes(random);
        String verifier = base64Url(random);                 // raw secret returned to FE
        String tokenHash = passwordEncoder.encode(verifier); // only the hash is stored

        // STEP 3: Enforce "one row per email".
        // Try to reuse existing row; if not found, create a new one.
        PasswordResetToken prt = null;
        try {
            // Requires: PasswordResetTokenRepository#findByEmail(String)
            prt = passwordResetTokenRepository.findByEmail(email).orElse(null);
        } catch (Exception ignore) {
            // If repository method is not available, prt will remain null and we will create a new row.
        }
        if (prt == null) {
            prt = new PasswordResetToken();
            prt.setEmail(email); // createdAt is handled by @PrePersist
        }

        // STEP 4: Refresh token state (upsert semantics).
        prt.setUsed(false);                                            // token is not consumed yet
        prt.setExpiresAt(Instant.now().plusSeconds(20 * 60));          // short TTL: 20 minutes
        prt.setTokenHash(tokenHash);                                   // store hash only, never the raw verifier

        // STEP 5: Persist once (INSERT or UPDATE depending on existence).
        prt = passwordResetTokenRepository.save(prt);

        // STEP 6: Compose public reset token "<id>.<verifier>" to return to FE (never store the raw verifier).
        String resetToken = prt.getId() + "." + verifier;
        return Optional.of(resetToken);
    }


    // -----------------------------------------------------------------------------------
    // Reset password by resetToken (format "<tokenId>.<verifier>")
    // -----------------------------------------------------------------------------------
    @Override
    @Transactional
    public boolean resetPassword(ResetPasswordRequest request) {
        // Basic payload validation
        if (request == null) return false;

        String resetToken = request.getResetToken();
        String newPassword = request.getNewPassword();

        if (resetToken == null || resetToken.isBlank() ||
                newPassword == null || newPassword.isBlank()) {
            return false;
        }

        // Format "<tokenId>.<verifier>"
        int dot = resetToken.indexOf('.');
        if (dot <= 0 || dot == resetToken.length() - 1) {
            throw new ResponseStatusException(BAD_REQUEST, "Invalid or expired token");
        }
        String tokenId = resetToken.substring(0, dot);
        String verifier = resetToken.substring(dot + 1);

        // Load token
        Optional<PasswordResetToken> opt = passwordResetTokenRepository.findById(tokenId);
        if (!opt.isPresent()) return false;

        PasswordResetToken prt = opt.get();
        if (prt.isUsed()) return false;
        if (prt.getExpiresAt() == null || prt.getExpiresAt().isBefore(Instant.now())) return false;

        // Verify verifier against stored hash
        boolean matches;
        try {
            matches = passwordEncoder.matches(verifier, prt.getTokenHash());
        } catch (Exception ex) {
            matches = false;
        }
        if (!matches) return false;

        // Consume token (one-time)
        prt.setUsed(true);
        passwordResetTokenRepository.save(prt);

        // Update password for the bound account
        Optional<Account> accOpt = accountRepository.findAccountByEmail(prt.getEmail());
        if (!accOpt.isPresent()) return false;

        Account acc = accOpt.get();
        acc.setPassword(passwordEncoder.encode(newPassword));
        accountRepository.save(acc);

        // Optional: revoke sessions if your repo supports it
        // (refreshTokenRepository.revokeAllByUserId(...) is not available now)

        return true;
    }
}
