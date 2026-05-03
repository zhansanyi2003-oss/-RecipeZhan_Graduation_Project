package org.zhan.recipe_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zhan.recipe_backend.common.Result;
import org.zhan.recipe_backend.service.RecipeTaxonomyService;

@RestController
@RequestMapping("/api/taxonomy")
public class RecipeTaxonomyController {

    @Autowired
    private RecipeTaxonomyService recipeTaxonomyService;

    @GetMapping("/flavours")
    public Result getFlavours() {
        return Result.Success(recipeTaxonomyService.getFlavours());
    }

    @GetMapping("/cuisines")
    public Result getCuisines() {
        return Result.Success(recipeTaxonomyService.getCuisines());
    }
}
