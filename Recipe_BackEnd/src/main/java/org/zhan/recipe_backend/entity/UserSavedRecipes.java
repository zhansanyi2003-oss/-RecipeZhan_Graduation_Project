package org.zhan.recipe_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_saved_recipes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_saved_user_recipe",
                        columnNames = {"user_id", "recipe_id"}
                )
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSavedRecipes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 关联食谱
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
