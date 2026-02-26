package org.zhan.recipe_backend.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.zhan.recipe_backend.common.Result;
import org.zhan.recipe_backend.dto.RecipeCardDto;
import org.zhan.recipe_backend.dto.RecipeDetailDto;
import org.zhan.recipe_backend.service.RecipeCardService;
import org.zhan.recipe_backend.service.RecipeService;

@RestController
@RequestMapping("/api/recipes")




public class recipeController {

    @Autowired
    private RecipeCardService recipeCardService;

    @Autowired
    private RecipeService recipeService;

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




}
