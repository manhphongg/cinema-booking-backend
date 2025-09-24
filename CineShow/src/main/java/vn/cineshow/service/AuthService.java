package vn.cineshow.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import vn.cineshow.dto.request.ChangePasswordRequest;
import vn.cineshow.dto.request.ForgotPasswordRequest;
import vn.cineshow.model.Account;
import vn.cineshow.model.User;
import vn.cineshow.repository.AccountRepository;
import vn.cineshow.repository.UserRepository;

import java.util.Optional;

@Service
public class AuthService {

    @Autowired
    private AccountRepository accountRepository;

    public boolean forgotPassword(ForgotPasswordRequest request) {
        Optional<UserDetails> optionalUserDetails = accountRepository.findByEmail(request.getEmail());

        if (optionalUserDetails.isPresent()) {
            Account account = (Account) optionalUserDetails.get(); // Cast
            account.setPassword(request.getNewPassword());
            accountRepository.save(account);
            return true;
        }

        return false;
    }

    public boolean changePassword(ChangePasswordRequest request) {
        Optional<UserDetails> optionalUserDetails = accountRepository.findByEmail(request.getEmail());

        if (optionalUserDetails.isPresent()) {
            Account account = (Account) optionalUserDetails.get(); // Cast
            if (account.getPassword().equals(request.getOldPassword())) {
                account.setPassword(request.getNewPassword());
                accountRepository.save(account);
                return true;
            }
        }

        return false;
    }
}
