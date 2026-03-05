package org.zhan.recipe_backend.dto;

import lombok.Data;

import java.util.List;

@Data
public class RecipeStepDto {
    private String content;
    private List<String> imageUrls;
}
