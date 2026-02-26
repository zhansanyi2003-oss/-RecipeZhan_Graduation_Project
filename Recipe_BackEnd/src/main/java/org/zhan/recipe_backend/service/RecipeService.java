package org.zhan.recipe_backend.service;

import org.zhan.recipe_backend.dto.RecipeDetailDto;
import org.zhan.recipe_backend.dto.UserDto;

public interface RecipeService {

    public RecipeDetailDto getRecipes(Long id);

    void addRecipe(RecipeDetailDto dto, Long userId);
}
