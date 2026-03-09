package org.zhan.recipe_backend.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zhan.recipe_backend.entity.Recipe;
import org.zhan.recipe_backend.entity.RecipeRating;
import org.zhan.recipe_backend.repository.RatingRepository;
import org.zhan.recipe_backend.repository.RecipeEsRepository;
import org.zhan.recipe_backend.repository.RecipeRepository;
import org.zhan.recipe_backend.service.RecipeCardService;

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

    @Transactional
    public Map<String, Object> deleteRating(Long id, Long userId) {
        RecipeRating existingRating = ratingRepository.findByUserIdAndRecipeId(id, userId)
                .orElseThrow(() -> new RuntimeException("您还未对该食谱打分，无法取消！"));
        ratingRepository.delete(existingRating);
        ratingRepository.flush();
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("食谱不存在"));

        // 去打分表里重新数一下这道菜还剩多少人打分
        int newCount = ratingRepository.countByRecipeId(id);
        Double newAvg;
        if (newCount > 0) {
            // 如果还有人打分，让数据库用 SQL 的 AVG() 函数重新算一次平均分
           newAvg= ratingRepository.calculateAverageByRecipeId(id);
            // 保留一位小数 (比如 4.5)
            newAvg = Math.round(newAvg * 10.0) / 10.0;


        } else {
          newAvg = 0.0;

        }
        recipe.setRatingCount(newCount);
        recipe.setAverageRating(newAvg);

        Map<String, Object> result = new HashMap<>();
        result.put("newAverageRating", newAvg);
        result.put("newRatingCount", newCount);
        Recipe savedRecipe = recipeRepository.save(recipe);
        recipeService.syncToElasticsearch(savedRecipe);


        return result;

    }
}