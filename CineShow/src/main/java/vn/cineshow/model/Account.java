package vn.cineshow.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import vn.cineshow.enums.UserStatus;

import java.io.Serializable;
import java.util.Collection;
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

    UserStatus status;

    @ManyToOne()
    @JoinColumn(name = "role_id")
    Role role;

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
    User user;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        //get Role

        //get role name

        //add role name to authority
        return List.of();
    }

    @Override
    public String getUsername() {
        return "";
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
        return UserDetails.super.isEnabled();
    }
}