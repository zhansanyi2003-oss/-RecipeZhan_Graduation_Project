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

    // ==========================================
    // 🌟 2. 登录接口
    // ==========================================
    @PostMapping("/login")
    public Result login(@RequestBody UserLoginDto dto) {


        try {
            // 1. 调用 Service 进行登录，并接收辛苦生成的 Token！
            // 🚨 修复Bug：必须把 userService 返回的 token 存起来
            String token = userService.loginUser(dto);

            // 2. 账号密码完全正确！把 Token 包装在 Result 的 data 里发给前端
            return Result.Success(token);

        } catch (AuthenticationException e) {
            // 3. 🌟 核心拦截：如果密码错误或账号不存在，Spring Security 抛出的异常会掉进这里！
            // 我们稳稳接住，然后用我们自己漂亮的 Result 格式返回给前端！
            return Result.Error("账号或密码错误，请重新输入！");
        }
    }


}