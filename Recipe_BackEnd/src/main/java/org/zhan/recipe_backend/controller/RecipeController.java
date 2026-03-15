package org.zhan.recipe_backend.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.zhan.recipe_backend.common.Result;
import org.zhan.recipe_backend.dto.RatingRequestDto;
import org.zhan.recipe_backend.dto.RecipeCardDto;
import org.zhan.recipe_backend.dto.RecipeDetailDto;
import org.zhan.recipe_backend.service.RecipeCardService;
import org.zhan.recipe_backend.service.RecipeService;
import org.zhan.recipe_backend.service.impl.RatingServiceImpl;

import java.time.LocalTime;
import java.util.Map;

@RestController
@RequestMapping("/api/recipes")
public class RecipeController {

    @Autowired
    private RecipeCardService recipeCardService;

    @Autowired
    private RecipeService recipeService;
    @Autowired
    private RatingServiceImpl ratingService;
    Map<String, Object> newStats;

    @PostMapping("")
    public Result getRecipe(@RequestBody RecipeCardDto dto, @RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "12") Integer pageSize) {

        return Result.Success(recipeCardService.getRecipeCards(dto, page, pageSize));
    }

    @GetMapping("/recommendations")
    public Result getRecommendations(@RequestParam(required = false) Integer hour) {
        int queryHour = hour != null ? hour : LocalTime.now().getHour();
        return Result.Success(recipeCardService.getPersonalizedRecommendations(queryHour));
    }

    @GetMapping("/flavours")
    public Result getFlavours() {

        return Result.Success(recipeCardService.getFlavours());
    }

    @GetMapping("/cuisines")
    public Result getCuisines() {

        return Result.Success(recipeCardService.getCuisines());
    }
    @GetMapping("/ingredients")
    public Result getIngredients() {

        return Result.Success(recipeCardService.getIngredients());
    }

    @GetMapping("/{id}")
    public Result getRecipeDetails(@PathVariable Long id) {
        return Result.Success(recipeService.getRecipes(id));


    }


    @GetMapping("/recc")
    public Result getReccomendRecipes(@RequestParam Integer localHour) {
        return Result.Success(recipeCardService.getPersonalizedRecommendations(localHour));

    }
    @GetMapping("/trending")
    public Result getTrendingRecipes(@RequestParam(defaultValue = "0") Integer page,@RequestParam(defaultValue = "12") Integer pageSize) {
        return Result.Success(recipeCardService.getTrendingRecipes(page,pageSize));

    }



    @DeleteMapping("/rate")
    public Result deleteRecipe(@RequestParam("id") Long id) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        newStats = ratingService.deleteRating(id, userId);
        return Result.Success(newStats);
    }

    @PostMapping("/create")
    public Result addRecipe(@RequestBody RecipeDetailDto dto) {
        recipeService.addRecipe(dto);
        return Result.Success();
    }

    @PutMapping("/{id}")
    public Result updateRecipe(@PathVariable Long id, @RequestBody RecipeDetailDto dto) {
        recipeService.updateRecipe(id, dto);
        return Result.Success();
    }

    @PostMapping("/rate")
    public Result submitRating(@RequestBody RatingRequestDto dto) {

        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            // 调用 Service，拿到最新的平均分和人数
            newStats = ratingService.submitRating(dto.getRecipeId(), userId, dto.getScore());
            return Result.Success(newStats);
        } catch (Exception e) {
            return Result.Error("Failed to submit rating");
        }
    }
}
