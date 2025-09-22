package vn.cineshow.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.cineshow.enums.UserStatus;

import java.io.Serializable;

@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Account extends AbstractEntity implements Serializable {
    String email;

    String password;

    UserStatus status;

    @OneToOne(mappedBy = "account", cascade = CascadeType.ALL)
    User user;

}