package org.zhan.recipe_backend.service;

import org.springframework.data.domain.Slice;

import org.zhan.recipe_backend.dto.RecipeDetailDto;
import org.zhan.recipe_backend.dto.RecipeRequestDto;

public interface RecipeService {

     RecipeDetailDto getRecipes(Long id);

    void addRecipe(RecipeRequestDto dto);

    void updateRecipe(Long id, RecipeRequestDto dto);

    void deleteRecipe(Long id);

}
