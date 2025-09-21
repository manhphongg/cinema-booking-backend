package vn.cineshow.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "movies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Movie extends AbstractEntity implements Serializable {

    @Column(nullable = false, length = 200)
    String name;

    @Column(columnDefinition = "TEXT")
    String description;

    @Column(nullable = false)
    int duration;
    @Column(nullable = false)
    LocalDate releaseDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;

    private String posterUrl;
    private String bannerUrl;
    private String trailerUrl;

    @ManyToMany()
    private Set<MovieGenre> movieGenres;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "movie")
    private List<ShowTime> showTimes;
}
