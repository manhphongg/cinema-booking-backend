package vn.cineshow.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.cineshow.dto.request.EmailRegisterRequest;
import vn.cineshow.enums.AccountStatus;
import vn.cineshow.exception.DuplicateResourceException;
import vn.cineshow.exception.ResourceNotFoundException;
import vn.cineshow.model.Account;
import vn.cineshow.model.Role;
import vn.cineshow.model.User;
import vn.cineshow.repository.AccountRepository;
import vn.cineshow.repository.RoleRepository;
import vn.cineshow.repository.UserRepository;
import vn.cineshow.service.OtpService;
import vn.cineshow.service.RegisterService;

@Service
@RequiredArgsConstructor
public class RegisterServiceImpl implements RegisterService {

    private static final long CUSTOMER_ROLE_ID = 3; // cố định như bạn yêu cầu

    private final AccountRepository accountRepo;
    private final RoleRepository roleRepo;
    private final UserRepository userRepo;
    private final PasswordEncoder encoder;
    private final OtpService otpService;

    @Transactional
    @Override
    public long registerByEmail(EmailRegisterRequest req) {
        // 0) xác nhận đã verify OTP
        if (!otpService.isEmailVerified(req.email())) {
            throw new IllegalStateException("Email has not been verified by OTP.");
        }

        // 1) chặn trùng email
        if (accountRepo.findByEmail(req.email()).isPresent()) {
            throw new DuplicateResourceException("Email already exists");
        }

        // 2) lấy role CUSTOMER theo ID
        Role customer = roleRepo.findById(CUSTOMER_ROLE_ID)
                .orElseThrow(() -> new ResourceNotFoundException("Role CUSTOMER not found"));

        // 3) tạo Account trước
        Account account = Account.builder()
                .email(req.email())
                .password(encoder.encode(req.password()))
                .status(AccountStatus.ACTIVE)
                .role(customer)
                .build();
        accountRepo.save(account);

        // 4) tạo User gắn FK đến Account (composition)
        User user = User.builder()
                .name(req.name())
                .dateOfBirth(req.dateOfBirth())
                .gender(req.gender())
                .account(account)
                .build();
        userRepo.save(user);

        // 5) dọn trạng thái OTP
        otpService.clearState(req.email());

        return account.getId();
    }
}


