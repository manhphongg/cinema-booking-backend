package vn.cineshow.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.cineshow.dto.request.EmailRegisterRequest;
import vn.cineshow.enums.AccountStatus;
import vn.cineshow.exception.AuthenticatedException;
import vn.cineshow.exception.DuplicateResourceException;
import vn.cineshow.exception.ResourceNotFoundException;
import vn.cineshow.model.Account;
import vn.cineshow.model.Role;
import vn.cineshow.model.User;
import vn.cineshow.repository.AccountRepository;
import vn.cineshow.repository.OtpCodeRepository;
import vn.cineshow.repository.RoleRepository;
import vn.cineshow.service.OtpService;
import vn.cineshow.service.RegisterService;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RegisterServiceImpl implements RegisterService {
    private final AccountRepository accountRepo;
    private final RoleRepository roleRepo;
    private final PasswordEncoder encoder;
    private final OtpService otpService;
    private final OtpCodeRepository otpCodeRepo;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    @Override
    public long registerByEmail(EmailRegisterRequest req) {

        Optional<Account> accountOtp = accountRepo.findAccountByEmail(req.email());

        // TH1: Đã có account
        if (accountOtp.isPresent()) {
            Account account = accountOtp.get();

            if (account.getStatus().equals(AccountStatus.ACTIVE)) {
                throw new DuplicateResourceException("Email already exists");
            } else if (account.getStatus().equals(AccountStatus.DEACTIVATED)) {
                throw new AuthenticatedException("Your account has been locked by administrator");
            }

            // Nếu đang PENDING → cập nhật lại user info + password, gửi OTP mới
            User user = account.getUser();
            if (user == null) {
                // Tạo mới user nếu DB đang bị thiếu (dirty data)
                user = new User();
                user.setAccount(account); // rất quan trọng: set ngược lại để Hibernate map quan hệ
            }
            user.setName(req.name());
            user.setDateOfBirth(req.dateOfBirth());
            user.setGender(req.gender());

            account.setUser(user);
            account.setPassword(encoder.encode(req.password()));
            account.setStatus(AccountStatus.PENDING); // reset lại trạng thái pending nếu cần
            accountRepo.save(account);

            otpService.sendOtp(req.email(), req.name());
            return account.getId();
        }

        // TH2: Chưa có account → tạo mới
        Role customer = roleRepo.findByRoleName("CUSTOMER")
                .orElseThrow(() -> new ResourceNotFoundException("Role CUSTOMER not found"));

        User user = User.builder()
                .name(req.name())
                .dateOfBirth(req.dateOfBirth())
                .gender(req.gender())
                .build();

        Account account = Account.builder()
                .email(req.email())
                .password(encoder.encode(req.password()))
                .status(AccountStatus.PENDING)
                .role(customer)
                .user(user)
                .build();

        // nhớ set ngược lại
        user.setAccount(account);

        accountRepo.save(account);

        otpService.sendOtp(req.email(), req.name());
        return account.getId();
    }


    public void verifyAccountAndUpdateStatus(String email, String otp) {
        Instant now = Instant.now();
        var code = otpCodeRepo.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("OTP not found. Please request a new code."));

        boolean ok = passwordEncoder.matches(otp, code.getOtpHash());
        if (!ok) {
            throw new IllegalArgumentException("Invalid OTP.");
        }

        if (code.getExpiresAt().isBefore(now)) {
            throw new IllegalStateException("OTP expired.");
        }

        code.setUsed(true);
        otpCodeRepo.save(code);

        Account account = accountRepo.findAccountByEmail(email).get();
        account.setStatus(AccountStatus.ACTIVE);
        accountRepo.save(account);
    }


}