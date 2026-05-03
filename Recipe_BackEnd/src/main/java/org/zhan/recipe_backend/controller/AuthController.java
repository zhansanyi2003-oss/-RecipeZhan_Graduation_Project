package org.zhan.recipe_backend.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zhan.recipe_backend.common.Result;
import org.zhan.recipe_backend.dto.UserLoginDto;
import org.zhan.recipe_backend.dto.UserSingUpDto;
import org.zhan.recipe_backend.service.UserService;

@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result register(@Valid @RequestBody UserSingUpDto dto) {

        if (!userService.registerUser(dto))
        {
            return Result.Error("Username or email is already in use");
        }
        return Result.Success();
    }


    @PostMapping("/login")
    public Result login(@Valid @RequestBody UserLoginDto dto) {


        try {
            String token = userService.loginUser(dto);

            return Result.Success(token);

        } catch (AuthenticationException e) {
            return Result.Error("账号或密码错误，请重新输入！");
        }
    }

    @PostMapping("/logout")
    public Result logout() {
        userService.deleteSession();
        return Result.Success();
    }
}
