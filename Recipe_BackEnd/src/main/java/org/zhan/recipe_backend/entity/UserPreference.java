package org.zhan.recipe_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.zhan.recipe_backend.common.CourseEnum;

import java.util.List;

@Entity
@Table(name = "user_preferences")
@Data
public class UserPreference {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // 告诉数据库这是外键
    private User user;

    // 如果你之前把 Course 定义成了 Enum，这里也直接用 Enum 数组
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "favorite_courses", columnDefinition = "varchar[]")
    private List<CourseEnum> favoriteCourses;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "favorite_flavours", columnDefinition = "varchar[]")
    private List<String> favoriteFlavours;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "favorite_cuisines", columnDefinition = "varchar[]")
    private List<String> favoriteCuisines;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "favorite_ingredients", columnDefinition = "varchar[]")
    private List<String> favoriteIngredients;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "disliked_ingredients", columnDefinition = "varchar[]")
    private List<String> dislikedIngredients; // 忌口和过敏
}