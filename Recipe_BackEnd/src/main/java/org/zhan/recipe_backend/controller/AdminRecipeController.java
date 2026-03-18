package org.zhan.recipe_backend.controller;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.zhan.recipe_backend.common.Result;
import org.zhan.recipe_backend.dto.RecipeDetailDto;
import org.zhan.recipe_backend.service.RecipeService;

@RestController
@RequestMapping("/api/admin/recipes")
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class AdminRecipeController {

    @Autowired
    private RecipeService recipeService;

    @GetMapping
    public Result getRecipeCards(
            @RequestParam(defaultValue = "0") @PositiveOrZero Integer page,
            @RequestParam(defaultValue = "12") @Min(1) @Max(50) Integer pageSize,
            @RequestParam(required = false) String keyword
    ) {
        return Result.Success(recipeService.getAdminRecipeCards(page, pageSize, keyword));
    }

    @PutMapping("/{id}")
    public Result updateRecipe(@PathVariable @Positive Long id, @RequestBody RecipeDetailDto dto) {
        recipeService.adminUpdateRecipe(id, dto);
        return Result.Success();
    }

    @DeleteMapping("/{id}")
    public Result deleteRecipe(@PathVariable @Positive Long id) {
        recipeService.adminDeleteRecipe(id);
        return Result.Success();
    }
}
