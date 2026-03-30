package org.zhan.recipe_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.zhan.recipe_backend.common.Result;
import org.zhan.recipe_backend.dto.UserLoginDto;
import org.zhan.recipe_backend.dto.UserSingUpDto;
import org.zhan.recipe_backend.entity.User;
import org.zhan.recipe_backend.service.impl.UserServiceImpl;
import org.zhan.recipe_backend.utils.JwtUtils; // 假设你有这个生成 JWT 的工具类

@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private UserServiceImpl userService;
    @PostMapping("/register")
    public Result register(@RequestBody UserSingUpDto dto) {

        if (!userService.registerUser(dto))
        {
            return Result.Error("The username is already in used");
        }
        return Result.Success();
    }


    @PostMapping("/login")
    public Result login(@RequestBody UserLoginDto dto) {


        try {
            String token = userService.loginUser(dto);

            return Result.Success(token);

        } catch (AuthenticationException e) {
            return Result.Error("账号或密码错误，请重新输入！");
        }
    }

    @PostMapping ("/logout")
    public Result logout() {
        userService.deleteSession();
        return Result.Success();
    }



}