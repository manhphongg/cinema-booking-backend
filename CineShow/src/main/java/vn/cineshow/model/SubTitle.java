package vn.cineshow.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.Serializable;

@Entity
@Table(name = "subtitles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SubTitle extends AbstractEntity implements Serializable {
    @Column(nullable = false, length = 10, unique = true)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

}
