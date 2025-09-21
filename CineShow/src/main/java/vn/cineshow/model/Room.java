package vn.cineshow.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.cineshow.enums.RoomStatus;
import java.io.Serializable;
import java.util.List;

@Entity(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Room extends AbstractEntity implements Serializable {

    String name;

    @ManyToOne(fetch = FetchType.LAZY)
    RoomType roomType;

    RoomStatus status;

    @OneToMany(mappedBy = "room",  cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    List<Seat> seats;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL,fetch = FetchType.LAZY)
    List<ShowTime> shows;


}
