package com.example.uberentityservice.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;

@Entity
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Color extends BaseModel{

    @Column(nullable = false, unique = true)
    private String name;
}
