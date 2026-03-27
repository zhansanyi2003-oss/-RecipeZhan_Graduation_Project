package org.zhan.recipe_backend.service;

import org.springframework.data.domain.Slice;
import org.zhan.recipe_backend.dto.RecipeCardDto;

import java.util.List;

public interface RecipeCardService {
    Slice<RecipeCardDto> getRecipeCards(RecipeCardDto cardParam, int page, int size) ;

    List<String> getFlavours();

    List<String>  getCuisines();

    List<String> getIngredients();
    List<RecipeCardDto> getHeroRecommendations(Integer hour);
    Slice<RecipeCardDto> getPersonalizedRecommendations(Integer hour, int page, int pageSize);
    Slice<RecipeCardDto> getBehaviorRecommendations(int page, int pageSize);
    Slice<RecipeCardDto> getTasteRecommendations(int page, int pageSize);


    Slice<RecipeCardDto> getTrendingRecipes(int page , int pageSize);
}
