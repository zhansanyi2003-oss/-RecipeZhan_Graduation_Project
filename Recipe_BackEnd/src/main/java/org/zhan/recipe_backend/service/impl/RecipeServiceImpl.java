package org.zhan.recipe_backend.service.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.zhan.recipe_backend.dto.RecipeDetailDto;
import org.zhan.recipe_backend.dto.UserDto;
import org.zhan.recipe_backend.entity.Recipe;
import org.zhan.recipe_backend.entity.User;
import org.zhan.recipe_backend.repository.RecipeRepository;
import org.zhan.recipe_backend.repository.UserRepository;
import org.zhan.recipe_backend.service.RecipeService;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class RecipeServiceImpl implements RecipeService {

    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private UserRepository userRepository;
    @Override
    public RecipeDetailDto getRecipes(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("哎呀，找不到这个食谱！ID: " + id));
        RecipeDetailDto dto = new RecipeDetailDto();
        BeanUtils.copyProperties(recipe, dto);

        return dto;
    }

    @Override
    public void addRecipe(RecipeDetailDto dto,  Long userId) {
        Recipe recipe = new Recipe();
        BeanUtils.copyProperties(dto, recipe);
        User author = userRepository.getReferenceById(userId);
        recipe.setAuthor(author); //
        recipe.setCreatedAt(LocalDateTime.now()); //
        recipe.setUpdatedAt(LocalDateTime.now());
        recipe.setLikesCount(0);
        recipeRepository.save(recipe);
    }
}
