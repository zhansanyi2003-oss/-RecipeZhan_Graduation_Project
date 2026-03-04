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
import org.zhan.recipe_backend.dto.RecipeCardDto;
import org.zhan.recipe_backend.dto.UserLoginDto;
import org.zhan.recipe_backend.dto.UserSingUpDto;
import org.zhan.recipe_backend.entity.Recipe;
import org.zhan.recipe_backend.entity.User;
import org.zhan.recipe_backend.entity.UserSavedRecipes;
import org.zhan.recipe_backend.repository.RecipeRepository;
import org.zhan.recipe_backend.repository.UserRepository;
import org.zhan.recipe_backend.repository.UserSavedRepository;
import org.zhan.recipe_backend.security.RecipeUserDetails;
import org.zhan.recipe_backend.service.UserService;
import org.zhan.recipe_backend.utils.AuthUtils;
import org.zhan.recipe_backend.utils.ConvertUtils;
import org.zhan.recipe_backend.utils.JwtUtils;

import java.time.LocalDateTime;
import java.util.List;
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

    @Override
    public List<RecipeCardDto> getMyRecipe() {
        Long userId =AuthUtils.getCurrentUserIdOrNull();

        List<Recipe> recipes= recipeRepository.findByAuthorIdOrderByCreatedAtDesc(AuthUtils.getCurrentUserIdOrNull());
        return recipes.stream()
                .map(recipe -> {
                    // 先做纯粹的数据类型转换
                    RecipeCardDto dto = ConvertUtils.convertToCardDto(recipe);

                    // 如果用户已登录，在 Service 层判断是否收藏
                    if (userId != null) {
                        boolean isSaved = userSavedRepository.existsByUserIdAndRecipeId(userId, recipe.getId());
                        dto.setIsLiked(isSaved);
                    }
                    return dto;
                })
                .collect(Collectors.toList());

    }

    @Transactional
    @Override
    public Boolean toggleSaveRecipe(Long recipeId,Boolean status) {
        Long userId =AuthUtils.getCurrentUserIdOrNull();
        if (status) {
            // 如果要收藏，且数据库没有，直接插入一条记录
            if (!userSavedRepository.existsByUserIdAndRecipeId(userId, recipeId)) {
                User user = userRepository.getReferenceById(userId); // 代理对象，不查库
                Recipe recipe = recipeRepository.getReferenceById(recipeId); // 代理对象，不查库

                UserSavedRecipes savedRecipe = new UserSavedRecipes();
                savedRecipe.setUser(user);
                savedRecipe.setRecipe(recipe);
                userSavedRepository.save(savedRecipe);
            }
            return true;
        } else {
            userSavedRepository.deleteByUserIdAndRecipeId(userId, recipeId);
            return false;
        }


    }

    @Override
    public Slice<RecipeCardDto> getSavedRecipe(Integer page,Integer pageSize) {

        int pageIndex = (page != null && page > 0) ? (page - 1) : 0;
        int size = (pageSize != null && pageSize > 0) ? pageSize : 12;
        Long userId=(Long) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Pageable pageable = PageRequest.of(pageIndex, size);
        Slice<Recipe> recipeSlice= userSavedRepository.findSavedRecipesByUserId(userId,pageable);
        return recipeSlice.map(recipe -> {
            RecipeCardDto dto = ConvertUtils.convertToCardDto(recipe);
            dto.setIsLiked(true);
            return dto;
        });
    }
}
