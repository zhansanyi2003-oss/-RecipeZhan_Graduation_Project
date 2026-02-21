package org.zhan.recipe_backend.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.zhan.recipe_backend.dto.RecipeCardDto;
import org.zhan.recipe_backend.projection.RecipeCardProjection;
import org.zhan.recipe_backend.repository.RecipeRepository;
import org.zhan.recipe_backend.service.RecipeCardService;

import java.util.List;


@Service
public class RecipeCardServiceImpl implements RecipeCardService {

    @Autowired
    private RecipeRepository recipeRepository;

    public Page<RecipeCardDto> getRecipeCards(RecipeCardDto cardParam,int page, int size) {

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<RecipeCardProjection> projectionPage = recipeRepository.findAllByOrderByCreatedAtDesc(cardParam,pageable);
        return projectionPage.map(proj -> {
            RecipeCardDto dto = new RecipeCardDto();
            dto.setId(proj.getId());
            dto.setTitle(proj.getTitle());
            dto.setCoverImage(proj.getCoverImage());
            dto.setDifficulty(proj.getDiff());
            dto.setCookingTimeMin(proj.getCookingTimeMin());
            dto.setIngredientTags(proj.getIngredientTags());
            dto.setLikesCount(proj.getLikesCount());
            dto.setCuisines(proj.getCuisines());
            dto.setFlavours(proj.getFlavours());

            return dto;
        });
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
