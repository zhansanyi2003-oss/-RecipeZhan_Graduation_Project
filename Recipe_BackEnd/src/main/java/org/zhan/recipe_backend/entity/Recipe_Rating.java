package org.zhan.recipe_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "recipe_ratings", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "recipe_id"})
})
@Data
public class Recipe_Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "recipe_id", nullable = false)
    private Long recipeId;

    // 评分：1到5星
    @Column(nullable = false)
    private Double score;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}