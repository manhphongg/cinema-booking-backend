package vn.cineshow.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Ticket extends AbstractEntity implements Serializable {

    @ManyToOne()
    Order order;

    @OneToOne
    Seat seat;

    @ManyToOne()
    ShowTime showTime;

    @Column(columnDefinition = "decimal(10,2)")
    Double ticketPrice;

    String qrCode;

}
