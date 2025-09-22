package vn.cineshow.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentMethod extends AbstractEntity implements Serializable {

    @Column(length = 50, nullable = false, unique = true)
    String methodCode;

    @Column(length = 50, nullable = false, unique = true)
    String methodName;

    boolean isActive;

    @OneToMany(mappedBy = "method")
    private List<Payment> payments;

}
