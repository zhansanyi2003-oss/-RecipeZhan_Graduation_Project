package org.zhan.recipe_backend.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class RatingRequestDto {
    @NotNull(message = "Recipe id is required")
    @Positive(message = "Recipe id must be positive")
    private Long recipeId;
    @NotNull(message = "Score is required")
    @DecimalMin(value = "0.5", message = "Score must be at least 0.5")
    @DecimalMax(value = "5.0", message = "Score must be at most 5.0")
    private Double score;
}
