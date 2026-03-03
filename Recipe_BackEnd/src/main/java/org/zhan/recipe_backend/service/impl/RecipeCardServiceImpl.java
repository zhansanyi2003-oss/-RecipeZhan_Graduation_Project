package org.zhan.recipe_backend.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.zhan.recipe_backend.dto.RecipeCardDto;
import org.zhan.recipe_backend.entity.Recipe;
import org.zhan.recipe_backend.repository.RecipeRepository;
import org.zhan.recipe_backend.service.RecipeCardService;

import java.util.List;


@Service
public class RecipeCardServiceImpl implements RecipeCardService {

    @Autowired
    private RecipeRepository recipeRepository;

    public Slice<RecipeCardDto> getRecipeCards(RecipeCardDto cardParam,int page, int size) {

        Pageable pageable = PageRequest.of(page - 1, size);
        Slice<Recipe> recipeSlice = recipeRepository.searchRecipesByParams(cardParam, cardParam.getMinTime(), cardParam.getMaxTime(),pageable);

        return recipeSlice.map(this::convertToCardDto);




    }
    private RecipeCardDto convertToCardDto(Recipe recipe) {
        RecipeCardDto dto = new RecipeCardDto();
        BeanUtils.copyProperties(recipe, dto);
        if (recipe.getCookingTimeMin() != null) {
            dto.setCookingTimeMin(String.valueOf(recipe.getCookingTimeMin()));
        }
        return dto;
    }

    @Override
    public List<String> getFlavours() {
        return recipeRepository.findAllDistinctFlavours();
    }

    @Override
    public List<String> getCuisines() {
        return  recipeRepository.findAllDistinctCuisines();
    }
}
