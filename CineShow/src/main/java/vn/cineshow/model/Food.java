package vn.cineshow.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "foods")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Food extends AbstractEntity implements Serializable {

    @Column(nullable = false, length = 100)
    String name;

    @Column(columnDefinition = "decimal(10,2)", nullable = false)
    Double price;

    String description;

    @OneToMany(mappedBy = "food", cascade = CascadeType.REMOVE, fetch = FetchType.LAZY)
    private List<OrderFood> orderFoods;

}
