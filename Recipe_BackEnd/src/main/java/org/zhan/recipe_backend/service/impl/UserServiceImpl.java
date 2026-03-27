package org.zhan.recipe_backend.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zhan.recipe_backend.common.RoleEnum;
import org.zhan.recipe_backend.dto.*;
import org.zhan.recipe_backend.entity.Recipe;
import org.zhan.recipe_backend.entity.User;
import org.zhan.recipe_backend.entity.UserSavedRecipes;
import org.zhan.recipe_backend.mapper.RecipeMapper;
import org.zhan.recipe_backend.mapper.UserMapper;
import org.zhan.recipe_backend.repository.RecipeRepository;
import org.zhan.recipe_backend.repository.UserRepository;
import org.zhan.recipe_backend.repository.UserSavedRepository;
import org.zhan.recipe_backend.security.RecipeUserDetails;
import org.zhan.recipe_backend.service.UserService;
import org.zhan.recipe_backend.utils.AuthUtils;
import org.zhan.recipe_backend.utils.ConvertUtils;
import org.zhan.recipe_backend.utils.JwtUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
    @Autowired
    private RecipeRepository recipeRepository;

    @Autowired
    private UserSavedRepository userSavedRepository;

    @Autowired
    private RecipeMapper recipeMapper;

    @Autowired
    private UserMapper userMapper;

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
        RecipeUserDetails userDetails = (RecipeUserDetails) auth.getPrincipal();
        User user = userDetails.getUser();
        // 3. 🌟 把 ID 塞进去生成 Token
        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), String.valueOf(user.getRole()));

        return "Bearer " + token;

    }

    @Override
    public Slice<RecipeCardDto> getMyRecipe(Integer page,Integer pageSize) {
        Long currentUserId =AuthUtils.getCurrentUserIdOrNull();
        Set<Long> userSavedRecipeIds = userSavedRepository.findRecipeIdsByUserId(currentUserId);
        Pageable pageable = PageRequest.of(page, pageSize);
        Slice<Recipe> recipes= recipeRepository.findByAuthorIdOrderByCreatedAtDesc(AuthUtils.getCurrentUserIdOrNull(),pageable);
        return recipes
                .map(recipe -> {// 先做纯粹的数据类型转换
                    RecipeCardDto dto = recipeMapper.toCardDto(recipe);
                    dto.setIsLiked(userSavedRecipeIds.contains(dto.getId()));
                    return dto;
                });

    }


    @Override
    public Slice<RecipeCardDto> getSavedRecipe(Integer page,Integer pageSize) {


        Long userId=(Long) AuthUtils.getCurrentUserIdOrNull();
        Pageable pageable = PageRequest.of(page, pageSize);
        Slice<Recipe> recipeSlice= userSavedRepository.findSavedRecipesByUserId(userId,pageable);
        return recipeSlice.map(recipe -> {
            RecipeCardDto dto = recipeMapper.toCardDto(recipe);
            dto.setIsLiked(true);
            return dto;
        });
    }

    @Transactional
    public void updateUserPreferences(UserPreferenceDto preferences) {
        Long userId = AuthUtils.getCurrentUserIdOrNull();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPreferences(preferences);
        userRepository.save(user);
    }

    public UserPreferenceDto getUserPreferences() {
        Long userId = AuthUtils.getCurrentUserIdOrNull();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));


        return user.getPreferences() != null ? user.getPreferences() : new UserPreferenceDto();
    }



    @Transactional
    @Override
    public Boolean toggleSaveRecipe(Long recipeId,Boolean status) {
        Long userId =AuthUtils.getCurrentUserIdOrNull();
        if (status) {
            userSavedRepository.saveRecipe(recipeId,userId);
            return true;
        } else {
            userSavedRepository.deleteByUserIdAndRecipeId(userId, recipeId);
            return false;
        }


    }

    @Override
    public UserDto getUserById() {
        Long currentUserId=AuthUtils.getCurrentUserIdOrNull();
        Optional<User> user=  userRepository.findById(currentUserId);
        UserDto userDto = userMapper.toUserDto(user.get());
        recipeRepository.countByAuthorId(currentUserId);
        userDto.setSavedCount(userSavedRepository.countByUserId(currentUserId));
        userDto.setCreatedCount(recipeRepository.countByAuthorId(currentUserId));

        return userDto;

    }

    @Override
    public void updateAvatar(String newAvatarUrl) {
        Long userId =AuthUtils.getCurrentUserIdOrNull();
        User user = userRepository.getReferenceById(userId);
        user.setAvatarUrl(newAvatarUrl);
        userRepository.save(user);
    }

    @Override
    public void deleteAvatar() {
        Long userId =AuthUtils.getCurrentUserIdOrNull();
        User user = userRepository.getReferenceById(userId);
        user.setAvatarUrl(null);
        userRepository.save(user);
    }
}
