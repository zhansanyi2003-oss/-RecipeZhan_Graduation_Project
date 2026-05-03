package org.zhan.recipe_backend.service.impl;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zhan.recipe_backend.dto.*;
import org.zhan.recipe_backend.entity.*;
import org.zhan.recipe_backend.exception.ForbiddenOperationException;
import org.zhan.recipe_backend.mapper.RecipeMapper;
import org.zhan.recipe_backend.repository.*;
import org.zhan.recipe_backend.service.RecipeSearchSyncService;
import org.zhan.recipe_backend.service.RecipeService;
import org.zhan.recipe_backend.utils.AuthUtils;

import java.util.Optional;

@Service
public class RecipeServiceImpl implements RecipeService {

    @Autowired
    private RecipeRepository recipeRepository;
    @Autowired
    private  UserRepository userRepository;

    @Autowired
    private  UserSavedRepository userSavedRepository;

    @Autowired
    private RecipeMapper recipeMapper;

    @Autowired
    private RecipeSearchSyncService recipeSearchSyncService;

    @Autowired
    private RecipeRelationAssembler recipeRelationAssembler;

    @Autowired
    private RatingRepository ratingRepository;

    @Override
    public RecipeDetailDto getRecipes(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("哎呀，找不到这个食谱！ID: " + id));
        RecipeDetailDto dto = recipeMapper.toDetailDto(recipe);
        Long currentUserId=AuthUtils.getCurrentUserIdOrNull();
        if ( currentUserId!= null) {
            boolean isLiked = userSavedRepository.existsByUserIdAndRecipeId(currentUserId, id);
            dto.setIsLiked(isLiked);
            Optional<Recipe_Rating> rating = ratingRepository.findByUserIdAndRecipeId(currentUserId, id);
            rating.ifPresent(r -> dto.setUserScore(r.getScore()));
        } else {
            dto.setIsLiked(false);
        }
        return dto;


    }

    @Transactional
    @Override
    public void addRecipe(RecipeRequestDto dto) {
        Recipe recipe = recipeMapper.toRecipe(dto);
        long currentUserId=AuthUtils.getCurrentUserIdOrNull();
        User author = userRepository.getReferenceById(currentUserId);
        recipe.setAuthor(author);
        recipeRelationAssembler.applyRelations(recipe, dto);
        Recipe savedRecipe = recipeRepository.save(recipe);
        recipeSearchSyncService.enqueueUpsert(savedRecipe.getId());
    }

    @Transactional
    @Override
    public void updateRecipe(Long id, RecipeRequestDto dto) {
        Long currentUserId = AuthUtils.getCurrentUserIdOrNull();

        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recipe not found, id: " + id));

        assertCanModifyRecipe(recipe, currentUserId);

        applyRecipeUpdate(recipe, dto);

        Recipe savedRecipe = recipeRepository.save(recipe);
        recipeSearchSyncService.enqueueUpsert(savedRecipe.getId());
    }

    @Transactional
    @Override
    public void deleteRecipe(Long id) {
        Long currentUserId = AuthUtils.getCurrentUserIdOrNull();

        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recipe not found, id: " + id));

        assertCanModifyRecipe(recipe, currentUserId);

        userSavedRepository.deleteByRecipeId(id);
        ratingRepository.deleteByRecipeId(id);
        recipeRepository.delete(recipe);
        recipeSearchSyncService.enqueueDelete(id);
    }

    private void applyRecipeUpdate(Recipe recipe, RecipeRequestDto dto) {
        recipeMapper.updateRecipeFromRequest(dto, recipe);
        recipeRelationAssembler.applyRelations(recipe, dto);
    }

    private void assertCanModifyRecipe(Recipe recipe, Long currentUserId) {
        boolean isOwner = recipe.getAuthor() != null && recipe.getAuthor().getId().equals(currentUserId);
        boolean isAdmin = AuthUtils.isAdmin();

        if (!isOwner && !isAdmin) {
            throw new ForbiddenOperationException("You can only modify your own recipe.");
        }
    }
}
