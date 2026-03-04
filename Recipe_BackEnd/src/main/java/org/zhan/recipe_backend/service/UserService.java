package org.zhan.recipe_backend.service;

import org.springframework.data.domain.Slice;
import org.zhan.recipe_backend.dto.RecipeCardDto;
import org.zhan.recipe_backend.dto.UserLoginDto;
import org.zhan.recipe_backend.dto.UserSingUpDto;
import org.zhan.recipe_backend.entity.User;

import java.util.List;
import java.util.Set;

public interface UserService {


     Boolean registerUser(UserSingUpDto dto);

     String loginUser(UserLoginDto user);

     List<RecipeCardDto> getMyRecipe();

     Boolean toggleSaveRecipe(Long recipeId, Boolean status);

     Slice<RecipeCardDto> getSavedRecipe(Integer page,Integer pageSize);
}
