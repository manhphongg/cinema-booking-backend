package vn.cineshow.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import vn.cineshow.enums.AccountStatus;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Account extends AbstractEntity implements Serializable, UserDetails {
    String email;

    String password;

    @Enumerated(EnumType.STRING)
    AccountStatus status;

    @ManyToOne()
    @JoinColumn(name = "role_id")
    Role role;

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
    User user;

    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
    List<AccountProvider> providers;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        //get Role
        Role userRole = role;
        //get role name
        String roleName = role.getRoleName();

        //add role name to authority
        return Collections.singleton(new SimpleGrantedAuthority(roleName));
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return AccountStatus.ACTIVE.equals(status);
    }
}