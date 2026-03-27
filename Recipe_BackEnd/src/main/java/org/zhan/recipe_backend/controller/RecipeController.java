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
    public Result getRecipe(@RequestBody RecipeCardDto dto, @RequestParam(defaultValue = "0") Integer page, @RequestParam(defaultValue = "12") Integer pageSize) {

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
    @GetMapping("/ingredients")
    public Result getIngredients() {

        return Result.Success(recipeCardService.getIngredients());
    }

    @GetMapping("/{id:\\d+}")
    public Result getRecipeDetails(@PathVariable Long id) {
        return Result.Success(recipeService.getRecipes(id));


    }


    @GetMapping("/recc")
    public Result getReccomendRecipes(@RequestParam Integer localHour,
                                      @RequestParam(defaultValue = "0") Integer page,
                                      @RequestParam(defaultValue = "12") Integer pageSize) {
        return Result.Success(recipeCardService.getPersonalizedRecommendations(localHour, page, pageSize));

    }

    @GetMapping("/hero")
    public Result getHeroRecommendations(@RequestParam Integer localHour) {
        return Result.Success(recipeCardService.getHeroRecommendations(localHour));
    }

    @GetMapping("/trending")
    public Result getTrendingRecipes(@RequestParam(defaultValue = "0") Integer page,@RequestParam(defaultValue = "12") Integer pageSize) {
        return Result.Success(recipeCardService.getTrendingRecipes(page,pageSize));

    }

    @GetMapping("/behavior")
    public Result getBehaviorRecommendations(@RequestParam(defaultValue = "0") Integer page,
                                             @RequestParam(defaultValue = "12") Integer pageSize) {
        return Result.Success(recipeCardService.getBehaviorRecommendations(page, pageSize));
    }

    @GetMapping("/taste")
    public Result getTasteRecommendations(@RequestParam(defaultValue = "0") Integer page,
                                          @RequestParam(defaultValue = "12") Integer pageSize) {
        return Result.Success(recipeCardService.getTasteRecommendations(page, pageSize));
    }



    @DeleteMapping("/rate")
    public Result deleteRecipe(@RequestParam("id") Long id) {
        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        return Result.Success(ratingService.deleteRating(id,userId));
    }

    @PostMapping("/create")
    public Result addRecipe(@RequestBody RecipeDetailDto dto) {
        recipeService.addRecipe(dto);
        return Result.Success();
    }

    @PutMapping("/{id:\\d+}")
    public Result updateRecipe(@PathVariable Long id, @RequestBody RecipeDetailDto dto) {
        recipeService.updateRecipe(id, dto);
        return Result.Success();
    }

    @PostMapping("/rate")
    public Result submitRating(@RequestBody RatingRequestDto dto) {

        Long userId = (Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        try {
            return Result.Success(ratingService.submitRating(dto.getRecipeId(), userId, dto.getScore()));
        } catch (Exception e) {
            return Result.Error("Failed to submit rating");
        }
    }
}
