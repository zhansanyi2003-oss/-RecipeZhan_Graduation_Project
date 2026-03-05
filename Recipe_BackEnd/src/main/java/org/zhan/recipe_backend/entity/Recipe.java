package org.zhan.recipe_backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import org.zhan.recipe_backend.common.CourseEnum;
import org.zhan.recipe_backend.common.DiffEnum;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "recipes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 这里可以改成 @ManyToOne 关联 User 实体，为了直观先用 Long
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id") // 告诉数据库，用 author_id 这个字段作为外键去连 users 表
    private User author;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "cover_image", nullable = false)
    private String coverImage;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private DiffEnum difficulty; // EASY, MEDIUM, HARD

    @Column(name = "cooking_time_min", nullable = false)
    private Integer cookingTimeMin;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stepNumber ASC")
    private List<RecipeStep> steps;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Recipe_Ingredient> ingredientsList;

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<Recipe_Course> recipeCourses = new ArrayList<>();

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<Recipe_Cuisine> recipeCuisines = new ArrayList<>();

    @OneToMany(mappedBy = "recipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @Builder.Default
    private List<Recipe_Flavour> recipeFlavours = new ArrayList<>();
    @Column(name = "average_rating")
    private Double averageRating;

    // 缓存的总评分人数 (默认 0)
    @Column(name = "rating_count")
    private Integer ratingCount;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
