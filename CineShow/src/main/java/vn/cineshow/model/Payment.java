package vn.cineshow.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.cineshow.enums.PaymentStatus;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity()
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "Payments")
public class Payment extends AbstractEntity implements Serializable {

    @ManyToOne
    Order order;

    @ManyToOne
    PaymentMethod method;

    @Column(nullable = false, columnDefinition = "DECIMAL(10,2)")
    Double amount;

    @Column(columnDefinition = "VARCHAR(100)")
    String transactionNo;

    @Column(columnDefinition = "VARCHAR(100)")
    String txnRef;

    @Enumerated(EnumType.STRING)
    PaymentStatus paymentStatus;

    LocalDateTime paymentDate;

}
