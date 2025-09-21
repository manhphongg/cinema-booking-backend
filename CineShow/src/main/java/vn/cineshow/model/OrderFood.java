package vn.cineshow.model;

import jakarta.persistence.*;
import lombok.*;
import vn.cineshow.model.ids.OrderFoodId;

import java.io.Serializable;

@Entity
@Table(name = "order_foods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderFood implements Serializable {

    @EmbeddedId
    private OrderFoodId orderFoodId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("orderId")
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("foodId")
    @JoinColumn(name = "food_id", nullable = false)
    private Food food;

    @Column(nullable = false)
    private int quantity;

    @Column(columnDefinition = "decimal(10,2)")
    private Double unitPrice = 0.00;
}
