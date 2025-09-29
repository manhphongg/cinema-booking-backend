package vn.cineshow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.cineshow.dto.request.ChangePasswordRequest;
import vn.cineshow.dto.request.ForgotPasswordRequest;
import vn.cineshow.model.Account;
import vn.cineshow.model.PasswordResetToken;
import vn.cineshow.repository.AccountRepository;
import vn.cineshow.repository.PasswordResetTokenRepository;
import vn.cineshow.service.impl.EmailService;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthService {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private EmailService mailSender;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Gửi OTP reset password qua email
     */
    public boolean forgotPassword(ForgotPasswordRequest request) {
        Optional<UserDetails> optionalUser = accountRepository.findByEmail(request.getEmail());

        if (optionalUser.isPresent()) {
            UserDetails userDetails = optionalUser.get();

            if (userDetails instanceof Account account) {
                // Sinh OTP
                String otp = String.format("%06d", new Random().nextInt(999999));
                
                // Log OTP ra console để test
                System.out.println("=== OTP FOR PASSWORD RESET ===");
                System.out.println("Email: " + account.getEmail());
                System.out.println("OTP: " + otp);
                System.out.println("Expiry: " + LocalDateTime.now().plusMinutes(5));
                System.out.println("==============================");

                // Lưu token
                PasswordResetToken token = PasswordResetToken.builder()
                        .otp(otp)
                        .expiry(LocalDateTime.now().plusMinutes(5))
                        .account(account)
                        .build();

                tokenRepository.save(token);

                // Gửi OTP qua email
                mailSender.sendOtpEmail(account.getEmail(), otp);

                return true;
            }
        }
        return false;
    }

    /**
     * Đổi mật khẩu khi biết mật khẩu cũ
     */
    public boolean changePassword(ChangePasswordRequest request) {
        Optional<UserDetails> optionalUserDetails = accountRepository.findByEmail(request.getEmail());

        if (optionalUserDetails.isPresent()) {
            Account account = (Account) optionalUserDetails.get();

            // So sánh mật khẩu cũ (dùng encoder.matches)
            if (passwordEncoder.matches(request.getOldPassword(), account.getPassword())) {
                account.setPassword(passwordEncoder.encode(request.getNewPassword()));
                accountRepository.save(account);
                return true;
            }
        }

        return false;
    }

    /**
     * Xác thực OTP và đặt lại mật khẩu mới
     */
    public boolean resetPasswordWithOtp(String email, String otp, String newPassword) {
        Optional<UserDetails> optionalUser = accountRepository.findByEmail(email);

        if (optionalUser.isPresent()) {
            UserDetails userDetails = optionalUser.get();

            if (userDetails instanceof Account account) {
                Optional<PasswordResetToken> optionalToken =
                        tokenRepository.findByAccountEmailAndOtp(email, otp);

                if (optionalToken.isPresent()) {
                    PasswordResetToken token = optionalToken.get();

                    if (token.getExpiry().isAfter(LocalDateTime.now())) {
                        // Cập nhật mật khẩu mới
                        account.setPassword(passwordEncoder.encode(newPassword));
                        accountRepository.save(account);

                        // Xoá token sau khi dùng
                        tokenRepository.delete(token);
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
