package org.zhan.recipe_backend.service;

import org.zhan.recipe_backend.dto.RatingStatsDto;

public interface RatingService {

    RatingStatsDto submitRating(Long recipeId, Long userId, Double score);

    RatingStatsDto deleteRating(Long recipeId, Long userId);
}
