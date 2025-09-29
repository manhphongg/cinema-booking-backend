package vn.cineshow.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.cineshow.model.User;
import vn.cineshow.repository.UserRepository;
import vn.cineshow.service.UserService;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public String getNameByAccountEmail(String email) {
        return userRepository.findByAccount_Email(email)
                .map(User::getName)
                .orElse("Friend");
    }
}
