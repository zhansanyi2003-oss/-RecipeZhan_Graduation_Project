package org.zhan.recipe_backend.dto;

import lombok.Data;
import org.zhan.recipe_backend.common.DiffEnum;

import java.time.LocalDateTime;

@Data
public class AdminRecipeCardDto {
    private Long id;
    private String title;
    private String coverImage;
    private DiffEnum difficulty;
    private Integer cookingTimeMin;
    private Double averageRating;
    private Integer ratingCount;
    private Long authorId;
    private String authorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
