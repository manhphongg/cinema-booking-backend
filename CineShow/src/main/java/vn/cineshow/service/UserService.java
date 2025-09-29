package vn.cineshow.service;

import vn.cineshow.dto.request.UpdateUserRequest;
import vn.cineshow.dto.response.UserResponse;

public interface UserService {

    UserResponse getProfile(String email);

    UserResponse updateProfile(String email, UpdateUserRequest request);
}
