package org.zhan.recipe_backend.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zhan.recipe_backend.entity.Recipe;
import org.zhan.recipe_backend.entity.RecipeRating;
import org.zhan.recipe_backend.repository.RatingRepository;
import org.zhan.recipe_backend.repository.RecipeEsRepository;
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


    @Transactional // 🌟 必须加事务，保证两张表同时成功或同时失败
    public  Map<String, Object> submitRating(Long recipeId, Long userId, Double score) {
        // 1. 查找用户是否已经评过分
        Optional<RecipeRating> existingRating = ratingRepository.findByUserIdAndRecipeId(userId, recipeId);

        RecipeRating rating;
        if (existingRating.isPresent()) {
            rating = existingRating.get();
            rating.setScore(score); // 更新分数
        } else {
            rating = new RecipeRating();
            rating.setUserId(userId);
            rating.setRecipeId(recipeId);
            rating.setScore(score); // 插入新分数
        }
        ratingRepository.save(rating);

        // 2. 重新计算这道菜的最新平均分和总人数
        Double newAvg = ratingRepository.calculateAverageByRecipeId(recipeId);
        Integer newCount = ratingRepository.countByRecipeId(recipeId);

        // 3. 更新到 Recipe 主表中
        Double formattedAvg = Math.round(newAvg * 10.0) / 10.0;
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new RuntimeException("Recipe not found"));
        // 保留一位小数
        recipe.setAverageRating(formattedAvg);
        recipe.setRatingCount(newCount);
        recipeRepository.save(recipe);

        Map<String, Object> result = new HashMap<>();
        result.put("newAverageRating", formattedAvg);
        result.put("newRatingCount", newCount);
        return result;
    }
}