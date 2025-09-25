package vn.cineshow.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.cineshow.enums.AuthProvider;
import vn.cineshow.model.Account;
import vn.cineshow.model.AccountProvider;

import java.util.Optional;

@Repository
public interface AccountProviderRepository extends JpaRepository<AccountProvider, Long> {
    Optional<AccountProvider> findByAccountAndProvider(Account account, AuthProvider provider);
}
