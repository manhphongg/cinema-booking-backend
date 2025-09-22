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

    // --- THAY ĐỔI Ở ĐÂY ---
    // 1. Xóa mappedBy = "user"
    // 2. Thêm @JoinColumn để tạo cột account_id trong bảng users
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "account_id", referencedColumnName = "id")
    Account account;

}