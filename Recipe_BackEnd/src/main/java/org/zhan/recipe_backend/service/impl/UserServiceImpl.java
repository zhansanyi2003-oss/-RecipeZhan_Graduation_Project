package org.zhan.recipe_backend.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.zhan.recipe_backend.common.Result;
import org.zhan.recipe_backend.common.RoleEnum;
import org.zhan.recipe_backend.dto.UserLoginDto;
import org.zhan.recipe_backend.dto.UserSingUpDto;
import org.zhan.recipe_backend.entity.User;
import org.zhan.recipe_backend.repository.UserRepository;
import org.zhan.recipe_backend.security.RecipeUserDetails;
import org.zhan.recipe_backend.service.UserService;
import org.zhan.recipe_backend.utils.JwtUtils;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtils jwtUtils;

    public Boolean registerUser(UserSingUpDto user ) {

        User newUser = new User();
        BeanUtils.copyProperties(user, newUser);
        User existingUser = userRepository.findByUsername(newUser.getUsername());
        if (existingUser != null) {
           return  false;
        }

        newUser.setPassword(passwordEncoder.encode(user.getPassword()));
        newUser.setRole(RoleEnum.USER);
        newUser.setCreatedAt(LocalDateTime.now());
        userRepository.save(newUser);
        return  true;

    }

    @Override
    public String loginUser(UserLoginDto userDto) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userDto.getUsername(), userDto.getPassword())
        );
        // 2. 账号密码对了，把用户信息拿出来
        RecipeUserDetails userDetails = (RecipeUserDetails) auth.getPrincipal();
        User user = userDetails.getUser();
        // 3. 🌟 把 ID 塞进去生成 Token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), String.valueOf(user.getRole()));

        return "Bearer " + token;

    }
}
