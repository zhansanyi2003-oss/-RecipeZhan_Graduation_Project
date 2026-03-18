package org.zhan.recipe_backend.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zhan.recipe_backend.dto.RatingStatsDto;
import org.zhan.recipe_backend.entity.Recipe;
import org.zhan.recipe_backend.entity.Recipe_Rating;
import org.zhan.recipe_backend.repository.RatingRepository;
import org.zhan.recipe_backend.repository.RecipeRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class RatingServiceImpl {

    @Autowired
    private RatingRepository ratingRepository;
    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private RecipeServiceImpl recipeService;


    @Transactional
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
                .orElseThrow(() -> new RuntimeException("Recipe not found"));

        recipe.setAverageRating(formattedAvg);
        recipe.setRatingCount(newCount);
        recipeRepository.save(recipe);
        recipeService.syncToElasticsearch(recipe);
        return new RatingStatsDto(newAvg,newCount);


    }

    @Transactional
    public RatingStatsDto deleteRating(Long id, Long userId) {
        Recipe_Rating existingRating = ratingRepository.findByUserIdAndRecipeId(userId, id)
                .orElseThrow(() -> new RuntimeException("您还未对该食谱打分，无法取消！"));
        ratingRepository.delete(existingRating);
        ratingRepository.flush();
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("食谱不存在"));

        // 去打分表里重新数一下这道菜还剩多少人打分
        int newCount = ratingRepository.countByRecipeId(id);
        Double newAvg=ratingRepository.getAverageByRecipeId(id);
        newAvg = Math.round(newAvg * 10.0) / 10.0;
        recipe.setRatingCount(newCount);
        recipe.setAverageRating(newAvg);
        return new RatingStatsDto(newAvg,newCount);

    }
}