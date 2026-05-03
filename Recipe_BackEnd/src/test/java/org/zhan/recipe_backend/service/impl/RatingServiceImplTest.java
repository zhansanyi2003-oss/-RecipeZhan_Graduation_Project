package org.zhan.recipe_backend.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.zhan.recipe_backend.dto.RatingStatsDto;
import org.zhan.recipe_backend.entity.Recipe;
import org.zhan.recipe_backend.repository.RatingRepository;
import org.zhan.recipe_backend.repository.RecipeRepository;
import org.zhan.recipe_backend.service.RecipeSearchSyncService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RatingServiceImplTest {

    @Test
    void submitRating_enqueuesSearchSyncAfterRatingStatsChange() {
        RatingServiceImpl service = new RatingServiceImpl();
        RatingRepository ratingRepository = mock(RatingRepository.class);
        RecipeRepository recipeRepository = mock(RecipeRepository.class);
        RecipeSearchSyncService recipeSearchSyncService = mock(RecipeSearchSyncService.class);
        Recipe recipe = Recipe.builder().id(42L).build();

        when(ratingRepository.findByUserIdAndRecipeId(7L, 42L)).thenReturn(Optional.empty());
        when(ratingRepository.getAverageByRecipeId(42L)).thenReturn(4.56);
        when(ratingRepository.countByRecipeId(42L)).thenReturn(3);
        when(recipeRepository.findById(42L)).thenReturn(Optional.of(recipe));
        when(recipeRepository.save(any(Recipe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReflectionTestUtils.setField(service, "ratingRepository", ratingRepository);
        ReflectionTestUtils.setField(service, "recipeRepository", recipeRepository);
        ReflectionTestUtils.setField(service, "recipeSearchSyncService", recipeSearchSyncService);

        RatingStatsDto result = service.submitRating(42L, 7L, 4.5);

        assertEquals(4.56, result.getNewAverageRating());
        assertEquals(3, result.getNewRatingCount());
        verify(recipeSearchSyncService).enqueueUpsert(42L);
    }
}
