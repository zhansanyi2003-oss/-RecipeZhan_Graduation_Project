package org.zhan.recipe_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "flavours")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Flavour {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;


}
