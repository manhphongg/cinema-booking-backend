package vn.cineshow.model;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Entity(name = "Users")
@Inheritance(strategy = InheritanceType.JOINED)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User extends AbstractEntity implements Serializable {

    String name;

    String address;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    Account account;

}
