package org.zhan.recipe_backend.dto;

import lombok.Data;
import org.zhan.recipe_backend.common.DiffEnum;

import java.util.List;

@Data
public class RecipeDetailDto {
    private Long id;
    private String title;
    private UserDto author;
    private String coverImage;
    private String description;
    private Integer cookingTimeMin;
    private DiffEnum difficulty;
    private List<String> flavours;
    private List<String> courses;
    private List<String> cuisines;
    private List<String>  dietTypes;
    private Double averageRating ;
    private Integer ratingCount;
    private Double userScore;
    private Boolean isLiked;
    private Long version;
    private List<IngredientDto> ingredients;
    private List<RecipeStepDto> steps;
}
