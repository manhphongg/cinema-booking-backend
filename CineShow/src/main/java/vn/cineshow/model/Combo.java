package vn.cineshow.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vn.cineshow.enums.ProductType;

import java.io.Serializable;

//@Entity
//@Setter
//@Builder
//@NoArgsConstructor
//@AllArgsConstructor
//@FieldDefaults(level = AccessLevel.PRIVATE)
public class Combo extends AbstractEntity implements Serializable {

    @Column(length = 100, nullable = false)
    String name;

    @Column(nullable = false, columnDefinition = "DECIMAL(10,2)")
    Double price;

    @Column(length = 255)
    String description;

    Boolean isActive = false;
}
