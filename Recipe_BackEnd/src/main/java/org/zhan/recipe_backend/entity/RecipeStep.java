package org.zhan.recipe_backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Entity
@Table(name = "recipe_steps")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🌟 核心 1：多对一关联，子表必须记住它属于哪个食谱！
    // 注意：这里加上 @EqualsAndHashCode.Exclude 和 @ToString.Exclude 防止我们之前聊过的循环依赖报错！
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    @lombok.EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private Recipe recipe;

    // 🌟 核心 2：记录这是第几步 (1, 2, 3...)，用于前端拖拽排序
    @Column(name = "step_number", nullable = false)
    private Integer stepNumber;

    // 对应前端的 element.content
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "image_urls", columnDefinition = "varchar[]")
    private List<String> imageUrls;
}