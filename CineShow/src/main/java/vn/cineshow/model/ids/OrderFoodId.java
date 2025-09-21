package vn.cineshow.model.ids;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class OrderFoodId {
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "food_id")
    private Long foodId;

}
