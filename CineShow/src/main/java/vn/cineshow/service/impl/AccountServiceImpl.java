package vn.cineshow.service.impl;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.cineshow.dto.request.EmailRegisterRequest;
import vn.cineshow.enums.AccountStatus;
import vn.cineshow.enums.AuthProvider;
import vn.cineshow.model.Account;
import vn.cineshow.model.AccountProvider;
import vn.cineshow.model.User;
import vn.cineshow.repository.AccountRepository;
import vn.cineshow.service.AccountService;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j(topic = "OTP-SERVICE")
class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

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

}
