package org.zhan.recipe_backend.service.impl;

import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.zhan.recipe_backend.dto.RecipeDetailDto;
import org.zhan.recipe_backend.dto.UserDto;
import org.zhan.recipe_backend.entity.Recipe;
import org.zhan.recipe_backend.entity.RecipeRating;
import org.zhan.recipe_backend.entity.User;
import org.zhan.recipe_backend.repository.RatingRepository;
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
    private RatingRepository ratingRepository;
    @Override
    public RecipeDetailDto getRecipes(Long id) {
        Recipe recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("哎呀，找不到这个食谱！ID: " + id));
        RecipeDetailDto dto = new RecipeDetailDto();
        BeanUtils.copyProperties(recipe, dto);


        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 如果没有认证信息，或者是框架默认的匿名用户（"anonymousUser"）
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return dto;
        }
        Long userId = (Long) authentication.getPrincipal();
        Optional<RecipeRating>  rating=ratingRepository.findByUserIdAndRecipeId(userId,id);
        rating.ifPresent(r -> dto.setUserScore(r.getScore()));

        return dto;
    }

    @Override
    public void addRecipe(RecipeDetailDto dto,  Long userId) {
        Recipe recipe = new Recipe();
        BeanUtils.copyProperties(dto, recipe);
        //User author = userRepository.getReferenceById(userId);
       // recipe.setAuthor(author); //
        recipe.setCreatedAt(LocalDateTime.now()); //
        recipe.setUpdatedAt(LocalDateTime.now());
        recipe.setLikesCount(0);
        recipeRepository.save(recipe);
    }
}
