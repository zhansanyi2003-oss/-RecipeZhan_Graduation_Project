package org.zhan.recipe_backend.service;

import org.springframework.data.domain.Slice;
import org.zhan.recipe_backend.dto.*;
import org.zhan.recipe_backend.entity.User;

import java.util.List;
import java.util.Set;

public interface UserService {


     Boolean registerUser(UserSingUpDto dto);

     String loginUser(UserLoginDto user);

     Slice<RecipeCardDto> getMyRecipe(Integer page,Integer pageSize);

     Boolean toggleSaveRecipe(Long recipeId, Boolean status);

     Slice<RecipeCardDto> getSavedRecipe(Integer page,Integer pageSize);

     UserDto getUserById();

     void updateAvatar(String newAvatarUrl);

     void deleteAvatar();
     UserPreferenceDto getUserPreferences();
     void updateUserPreferences(UserPreferenceDto preferences);

     void deleteSession();
}
