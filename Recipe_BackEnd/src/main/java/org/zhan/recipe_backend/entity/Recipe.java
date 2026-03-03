package org.zhan.recipe_backend.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import org.zhan.recipe_backend.common.CourseEnum;
import org.zhan.recipe_backend.common.DiffEnum;

import java.time.LocalDateTime;
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


    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "flavours", columnDefinition = "varchar[]")
    private List<String> flavours;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "courses", columnDefinition = "varchar[]") // 如果是 Postgres 用 jsonb，MySQL 用 json
    private List<String> courses;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "cuisines", columnDefinition = "varchar[]")
    private List<String> cuisines;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "ingredient_tags", columnDefinition = "varchar[]")
    private List<String> ingredientTags; // 专门用于"清冰箱"搜索的纯文本标签

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "ingredients_list", columnDefinition = "jsonb")
    private List<IngredientDetail> ingredientsList; // 复杂的食材明细

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "steps", columnDefinition = "jsonb")
    private List<RecipeStep> steps;


    @Column(name = "likes_count")
    private Integer likesCount;

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
