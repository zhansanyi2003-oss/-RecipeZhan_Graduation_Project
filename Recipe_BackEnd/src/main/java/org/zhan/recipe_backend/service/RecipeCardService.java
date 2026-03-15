package org.zhan.recipe_backend.service;

import org.springframework.data.domain.Slice;
import org.zhan.recipe_backend.dto.RecipeCardDto;

import java.util.List;

public interface RecipeCardService {
    Slice<RecipeCardDto> getRecipeCards(RecipeCardDto cardParam, int page, int size) ;

    List<String> getFlavours();

    List<String>  getCuisines();

    List<String> getIngredients();
    List<RecipeCardDto> getPersonalizedRecommendations(Integer hour);


    Slice<RecipeCardDto> getTrendingRecipes(int page , int pageSize);
}