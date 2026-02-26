package org.zhan.recipe_backend.dto;

import lombok.Data;
import org.zhan.recipe_backend.common.CourseEnum;
import org.zhan.recipe_backend.common.DiffEnum;
import org.zhan.recipe_backend.entity.IngredientDetail;
import org.zhan.recipe_backend.entity.RecipeStep;

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
    private List<IngredientDetail> ingredientsList;
    private List<RecipeStep> steps;
}