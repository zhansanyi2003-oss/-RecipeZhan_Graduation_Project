package org.zhan.recipe_backend.dto;

import lombok.Data;

@Data
public class RatingRequestDto {
    private Long recipeId;
    private Double score;
}