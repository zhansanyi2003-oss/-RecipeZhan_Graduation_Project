package org.zhan.recipe_backend.utils;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.zhan.recipe_backend.dto.RecipeCardDto;
import org.zhan.recipe_backend.entity.Recipe;
import org.zhan.recipe_backend.repository.UserSavedRepository;

@Component
public  class ConvertUtils {

    @Autowired
    private  UserSavedRepository userSavedRepository;
    public static RecipeCardDto convertToCardDto(Recipe recipe) {
        RecipeCardDto dto = new RecipeCardDto();
        BeanUtils.copyProperties(recipe, dto);
        if (recipe.getCookingTimeMin() != null) {
            dto.setCookingTimeMin(String.valueOf(recipe.getCookingTimeMin()));
        }
        dto.setAverageRating(recipe.getAverageRating() != null ? recipe.getAverageRating() : 0.0);
        dto.setRatingCount(recipe.getRatingCount() != null ? recipe.getRatingCount() : 0);
        dto.setIsLiked(false);
       return dto;


    }
}
