package vn.cineshow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.cineshow.model.OtpCode;
import java.util.Optional;

public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {
    Optional<OtpCode> findByEmail(String email);
    void deleteByEmail(String email); }