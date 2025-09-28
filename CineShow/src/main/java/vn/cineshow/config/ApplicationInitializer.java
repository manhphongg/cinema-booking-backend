package vn.cineshow.config;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import vn.cineshow.dto.request.AccountCreationRequest;
import vn.cineshow.enums.AccountStatus;
import vn.cineshow.enums.UserRole;
import vn.cineshow.exception.ResourceNotFoundException;
import vn.cineshow.model.Account;
import vn.cineshow.model.Role;
import vn.cineshow.model.User;
import vn.cineshow.repository.AccountRepository;
import vn.cineshow.repository.RoleRepository;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ApplicationInitializer {

    RoleRepository roleRepository;
    AccountRepository accountRepository;
    PasswordEncoder passwordEncoder;

    @Value("${admin.email_default}")
    @NonFinal
    String EMAIL_ADMIN;

    @Value("${admin.password_default}")
    @NonFinal
    String PASSWORD_ADMIN;

    @Value("${user-account-test.email}")
    @NonFinal
    String EMAIL_USER;

    @Value("${user-account-test.password}")
    @NonFinal
    String PASSWORD_USER;

    @Value("${staff-account-test.email}")
    @NonFinal
    String EMAIL_STAFF;

    @Value("${staff-account-test.password}")
    @NonFinal
    String PASSWORD_STAFF;

    @Value("${manager-account-test.email}")
    @NonFinal
    String EMAIL_MANAGER;

    @Value("${manager-account-test.password}")
    @NonFinal
    String PASSWORD_MANAGER;

    @Bean
    public ApplicationRunner initData() {
        return args -> {
            initRoles();
            initAccounts();
        };
    }

    void initRoles() {
        List<Role> roles = List.of(
                new Role("ADMIN", "The administrator"),
                new Role("CUSTOMER", "The customer using system"),
                new Role("MANAGER", "The manager using system"),
                new Role("STAFF", "The staff using system")
        );

        roles.forEach(role -> {
            roleRepository.findByRoleName(role.getRoleName())
                    .orElseGet(() -> {
                        log.info("Initialized role: {}", role.getRoleName());
                        return roleRepository.save(role);
                    });
        });
    }

    void initAccounts() {

        AccountCreationRequest admin = new AccountCreationRequest(EMAIL_ADMIN, PASSWORD_ADMIN, "System Admin", "Ha Noi");
        AccountCreationRequest customer = new AccountCreationRequest(EMAIL_USER, PASSWORD_USER, "System Customer", "Da Nang");
        AccountCreationRequest staff = new AccountCreationRequest(EMAIL_STAFF, PASSWORD_STAFF, "System Staff", "Ho Chi Minh");
        AccountCreationRequest manager = new AccountCreationRequest(EMAIL_MANAGER, PASSWORD_MANAGER, "System Manager", "Hai Phong");

        createAccountIfNotExists(admin, UserRole.ADMIN.name());
        createAccountIfNotExists(customer, UserRole.CUSTOMER.name());
        createAccountIfNotExists(staff, UserRole.STAFF.name());
        createAccountIfNotExists(manager, UserRole.MANAGER.name());
    }

    void createAccountIfNotExists(AccountCreationRequest request, String roleName) {
        if (accountRepository.findByEmail(request.getEmail()).isPresent()) {
            log.warn("Account already exists: {}", request.getEmail());
            return;
        }

        Role role = roleRepository.findByRoleName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        User user = User.builder()
                .name(request.getName())
                .address(request.getAddress())
                .loyalPoint(0)
                .build();

        Account account = Account.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .user(user)
                .status(AccountStatus.ACTIVE)
                .build();

        user.setAccount(account);

        accountRepository.save(account);
        log.info("Created account: {}", request.getEmail());
    }
}
