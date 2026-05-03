package org.zhan.recipe_backend.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zhan.recipe_backend.dto.RatingStatsDto;
import org.zhan.recipe_backend.entity.Recipe;
import org.zhan.recipe_backend.entity.Recipe_Rating;
import org.zhan.recipe_backend.exception.ConflictException;
import org.zhan.recipe_backend.exception.ResourceNotFoundException;
import org.zhan.recipe_backend.repository.RatingRepository;
import org.zhan.recipe_backend.repository.RecipeRepository;
import org.zhan.recipe_backend.service.RatingService;
import org.zhan.recipe_backend.service.RecipeSearchSyncService;

import java.util.Optional;

@Service
public class RatingServiceImpl implements RatingService {

    @Autowired
    private RatingRepository ratingRepository;
    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private RecipeSearchSyncService recipeSearchSyncService;


    @Transactional
    @Override
    public  RatingStatsDto submitRating(Long recipeId, Long userId, Double score) {

        Optional<Recipe_Rating> existingRating = ratingRepository.findByUserIdAndRecipeId(userId, recipeId);

        Recipe_Rating rating;
        if (existingRating.isPresent()) {
            rating = existingRating.get();
            rating.setScore(score);
        } else {
            rating = new Recipe_Rating();
            rating.setUserId(userId);
            rating.setRecipeId(recipeId);
            rating.setScore(score);
        }
        ratingRepository.save(rating);


        Double newAvg = ratingRepository.getAverageByRecipeId(recipeId);
        Integer newCount = ratingRepository.countByRecipeId(recipeId);

        Double formattedAvg = Math.round(newAvg * 10.0) / 10.0;
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe not found"));

        recipe.setAverageRating(formattedAvg);
        recipe.setRatingCount(newCount);
        Recipe savedRecipe = recipeRepository.save(recipe);
        recipeSearchSyncService.enqueueUpsert(savedRecipe.getId());
        return new RatingStatsDto(newAvg,newCount);


    }

    @Transactional
    @Override
    public RatingStatsDto deleteRating(Long id, Long userId) {
        Recipe_Rating existingRating = ratingRepository.findByUserIdAndRecipeId(userId, id)
                .orElseThrow(() -> new ConflictException("You have not rated this recipe yet."));
        ratingRepository.delete(existingRating);
        ratingRepository.flush();
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe not found"));

        // 去打分表里重新数一下这道菜还剩多少人打分
        int newCount = ratingRepository.countByRecipeId(id);
        Double newAvg=ratingRepository.getAverageByRecipeId(id);
        newAvg = Math.round(newAvg * 10.0) / 10.0;
        recipe.setRatingCount(newCount);
        recipe.setAverageRating(newAvg);
        Recipe savedRecipe = recipeRepository.save(recipe);
        recipeSearchSyncService.enqueueUpsert(savedRecipe.getId());
        return new RatingStatsDto(newAvg,newCount);

    }
}
