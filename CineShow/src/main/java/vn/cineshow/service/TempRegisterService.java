package vn.cineshow.service;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Service;
import vn.cineshow.dto.request.EmailRegisterRequest;

//@author Huyenpublic

public interface TempRegisterService {
public void saveRegisterData(EmailRegisterRequest req);
public EmailRegisterRequest getRegisterData(String email);
public void deleteRegisterData(String email);
public boolean emailExists(String email);
}
