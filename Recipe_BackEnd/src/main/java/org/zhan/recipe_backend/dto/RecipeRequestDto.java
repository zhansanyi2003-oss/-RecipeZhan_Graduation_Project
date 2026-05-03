package org.zhan.recipe_backend.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.zhan.recipe_backend.common.CourseEnum;
import org.zhan.recipe_backend.common.DiffEnum;
import org.zhan.recipe_backend.common.DietTypeEnum;

import java.util.List;

@Data
public class RecipeRequestDto {
    @NotBlank(message = "Recipe title is required")
    @Size(max = 100, message = "Recipe title must be at most 100 characters")
    private String title;

    @NotBlank(message = "Cover image is required")
    private String coverImage;

    private String description;

    @NotNull(message = "Cooking time is required")
    @Positive(message = "Cooking time must be positive")
    private Integer cookingTimeMin;

    @NotNull(message = "Difficulty is required")
    private DiffEnum difficulty;

    private List<String> flavours;
    private List<CourseEnum> courses;
    private List<String> cuisines;
    private List<DietTypeEnum> dietTypes;

    @Valid
    private List<IngredientDto> ingredients;

    @Valid
    private List<RecipeStepDto> steps;
}
