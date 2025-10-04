package vn.cineshow.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;

import java.io.Serializable;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends AbstractEntity implements Serializable {

    @Column(nullable = false, unique = true)
    private String roleName;
    
    private String description;
}
