package org.zhan.recipe_backend.utils;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.zhan.recipe_backend.dto.RecipeCardDto;
import org.zhan.recipe_backend.entity.Recipe;
import org.zhan.recipe_backend.repository.UserSavedRepository;

import java.util.List;
import java.util.stream.Collectors;

@Component
public  class ConvertUtils {

    @Autowired
    private  UserSavedRepository userSavedRepository;
    public static RecipeCardDto convertToCardDto(Recipe recipe) {


            RecipeCardDto dto = new RecipeCardDto();

            // 1. 基本属性（id, title, coverImage 等同名同类型字段）依然可以用 BeanUtils 自动拷贝
            BeanUtils.copyProperties(recipe, dto);

            // 2. 特殊字段手动处理
            if (recipe.getCookingTimeMin() != null) {
                dto.setCookingTimeMin(String.valueOf(recipe.getCookingTimeMin()));
            }
            dto.setAverageRating(recipe.getAverageRating() != null ? recipe.getAverageRating() : 0.0);
            dto.setRatingCount(recipe.getRatingCount() != null ? recipe.getRatingCount() : 0);
            dto.setIsLiked(false);

            // 提取 Flavours
            if (recipe.getRecipeFlavours() != null) {
                List<String> flavourNames = recipe.getRecipeFlavours().stream()
                        .map(rf -> rf.getFlavour().getName()) // 深入两层，拿到字典表里的名字
                        .collect(Collectors.toList());
                dto.setFlavours(flavourNames);
            }

            // 提取 Courses
            if (recipe.getRecipeCourses() != null) {
                List<String> courseNames = recipe.getRecipeCourses().stream()
                        .map(rc -> rc.getCourse().getName())
                        .collect(Collectors.toList());
                dto.setCourses(courseNames);
            }

            // 提取 Cuisines
            if (recipe.getRecipeCuisines() != null) {
                List<String> cuisineNames = recipe.getRecipeCuisines().stream()
                        .map(rc -> rc.getCuisine().getName())
                        .collect(Collectors.toList());
                dto.setCuisines(cuisineNames);
            }
            if (recipe.getIngredientsList() != null) {
                List<String> ingredientNames = recipe.getIngredientsList().stream()
                        .map(ri -> ri.getIngredient().getName())
                        .collect(Collectors.toList());
                dto.setIngredientTags(ingredientNames);
            }

            return dto;
        }



}
