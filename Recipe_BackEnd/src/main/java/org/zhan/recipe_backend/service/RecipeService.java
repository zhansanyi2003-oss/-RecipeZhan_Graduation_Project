package org.zhan.recipe_backend.service;

import org.springframework.data.domain.Slice;
import org.zhan.recipe_backend.dto.AdminRecipeCardDto;
import org.zhan.recipe_backend.dto.RecipeDetailDto;

public interface RecipeService {

    public RecipeDetailDto getRecipes(Long id);

    void addRecipe(RecipeDetailDto dto);

    void updateRecipe(Long id, RecipeDetailDto dto);

    Slice<AdminRecipeCardDto> getAdminRecipeCards(Integer page, Integer pageSize, String keyword);

    void adminUpdateRecipe(Long id, RecipeDetailDto dto);

    void adminDeleteRecipe(Long id);
}
