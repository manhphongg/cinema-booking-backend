package vn.cineshow.service.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.cineshow.dto.request.EmailRegisterRequest;
import vn.cineshow.dto.request.ForgotPasswordRequest;
import vn.cineshow.dto.request.ResetPasswordRequest;
import vn.cineshow.enums.AccountStatus;
import vn.cineshow.enums.AuthProvider;
import vn.cineshow.model.Account;
import vn.cineshow.model.AccountProvider;
import vn.cineshow.model.OtpCode;
import vn.cineshow.model.User;
import vn.cineshow.repository.AccountRepository;
import vn.cineshow.repository.OtpCodeRepository;
import vn.cineshow.service.AccountService;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "OTP-SERVICE")
class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final OtpCodeRepository otpCodeRepository;
    private final EmailService mailSender;
    private final PasswordEncoder passwordEncoder;
    private final OtpServiceImpl otpService;

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

    @Override
    public boolean forgotPassword(ForgotPasswordRequest req) {
        Optional<Account> accountOpt = accountRepository.findAccountByEmail(req.getEmail());
        if (accountOpt.isEmpty()) {
            return false;
        }

        Account account = accountOpt.get();
        otpService.sendOtp(account.getEmail(), account.getUser().getName());
        return true;
    }

    @Override
    public boolean verifyOtpForReset(String email, String otp) {
        return otpCodeRepository.findByEmail(email)
                .filter(token -> !token.isUsed())
                .filter(token -> token.getExpiresAt().isAfter(Instant.now()))
                .filter(token -> passwordEncoder.matches(otp, token.getOtpHash()))
                .isPresent();
    }

    @Override
    public boolean resetPassword(ResetPasswordRequest request) {
        Optional<Account> accountOpt = accountRepository.findAccountByEmail(request.getEmail());
        if (accountOpt.isEmpty()) return false;

        Optional<OtpCode> tokenOpt = otpCodeRepository.findByEmail(request.getEmail());
        if (tokenOpt.isEmpty()) return false;

        OtpCode token = tokenOpt.get();
        if (token.isUsed() || token.getExpiresAt().isBefore(Instant.now())) return false;

        Account account = accountOpt.get();
        account.setPassword(passwordEncoder.encode(request.getNewPassword()));
        accountRepository.save(account);

        // mark used hoặc xóa token luôn
        otpCodeRepository.delete(token);
        return true;
    }
}
