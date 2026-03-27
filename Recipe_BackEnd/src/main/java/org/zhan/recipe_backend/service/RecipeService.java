package org.zhan.recipe_backend.service;

import org.springframework.data.domain.Slice;

import org.zhan.recipe_backend.dto.RecipeDetailDto;

public interface RecipeService {

     RecipeDetailDto getRecipes(Long id);

    void addRecipe(RecipeDetailDto dto);

    void updateRecipe(Long id, RecipeDetailDto dto);

}
