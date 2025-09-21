package vn.cineshow.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Table(name = "showtimes")
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShowTime extends AbstractEntity implements Serializable {

    @Column(nullable = false)
    LocalDateTime startTime;

    @Column(nullable = false)
    LocalDateTime  endTime;

    @ManyToOne(fetch = FetchType.LAZY)
    Room room;

    @OneToMany(mappedBy = "showTime", fetch = FetchType.LAZY)
    private List<Ticket> tickets;

    @ManyToOne
    Movie movie;

    @OneToMany(mappedBy = "showTime", fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    private List<TicketPrice> ticketPrices;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subtitle_id", nullable = false)
    private SubTitle subtitle;
}
