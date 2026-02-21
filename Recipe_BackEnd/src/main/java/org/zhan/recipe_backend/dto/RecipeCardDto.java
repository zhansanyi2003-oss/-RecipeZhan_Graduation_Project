package org.zhan.recipe_backend.dto;

import lombok.Data;
import org.zhan.recipe_backend.common.DiffEnum;

import java.util.List;

@Data
public class RecipeCardDto {


    private Long id;
    private String title;
    private String coverImage;
    private DiffEnum difficulty;
    private Integer cookingTimeMin;
    private List<String> flavours;
    private List<String> cuisines;
    private List<String> ingredientTags;
    private List<String> courses;
    private Integer likesCount;
}
