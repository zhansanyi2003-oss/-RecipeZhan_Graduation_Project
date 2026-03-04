package org.zhan.recipe_backend.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.zhan.recipe_backend.common.Result;
import org.zhan.recipe_backend.dto.RatingRequestDTO;
import org.zhan.recipe_backend.dto.RecipeCardDto;
import org.zhan.recipe_backend.dto.RecipeDetailDto;
import org.zhan.recipe_backend.service.RecipeCardService;
import org.zhan.recipe_backend.service.RecipeService;
import org.zhan.recipe_backend.service.impl.RatingServiceImpl;

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

    @PostMapping("")
    public Result getRecipe(@RequestBody RecipeCardDto dto, @RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "12") Integer pageSize) {

        return Result.Success(recipeCardService.getRecipeCards(dto, page, pageSize));
    }

    @GetMapping("/flavours")
    public Result getFlavours() {

        return Result.Success(recipeCardService.getFlavours());
    }

    @GetMapping("/cuisines")
    public Result getCuisines() {

        return Result.Success(recipeCardService.getCuisines());
    }

    @GetMapping("/{id}")
    public Result getRecipeDetails(@PathVariable Long id) {
        return Result.Success(recipeService.getRecipes(id));


    }

    @PostMapping("/create")
    public Result addRecipe(@RequestBody RecipeDetailDto dto)
    {
        recipeService.addRecipe(dto,1l);
        return  Result.Success();
    }
    @PostMapping("/rate")
    public Result submitRating(@RequestBody RatingRequestDTO dto) {

        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            // 调用 Service，拿到最新的平均分和人数
            Map<String, Object> newStats = ratingService.submitRating(dto.getRecipeId(), userId, dto.getScore());
            return Result.Success(newStats);
        } catch (Exception e) {
            return Result.Error("Failed to submit rating");
        }
    }




}
