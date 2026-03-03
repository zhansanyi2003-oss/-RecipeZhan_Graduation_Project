package org.zhan.recipe_backend.service;

import org.zhan.recipe_backend.dto.UserLoginDto;
import org.zhan.recipe_backend.dto.UserSingUpDto;
import org.zhan.recipe_backend.entity.User;

public interface UserService {


    public Boolean registerUser(UserSingUpDto dto );
    public String  loginUser(UserLoginDto user);
}
