package vn.cineshow.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Voucher extends AbstractEntity implements Serializable {

    @Column(nullable = false, unique = true, columnDefinition = "VARCHAR(50)")
    String code;

    int discount;

    @Column(length = 255)
    String description;

    int maxUsage;

    int currentUsage;

    LocalDate startDate;

    LocalDate endDate;

    @ManyToOne
    Customer customer;

    @Column(nullable = false)
    boolean isActive =false;

    boolean isDeleted =false;

}
