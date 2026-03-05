package org.zhan.recipe_backend.service.impl;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.zhan.recipe_backend.dto.RecipeCardDto;
import org.zhan.recipe_backend.entity.*;
import org.zhan.recipe_backend.repository.CuisineRepository;
import org.zhan.recipe_backend.repository.FlavourRepository;
import org.zhan.recipe_backend.repository.RecipeRepository;
import org.zhan.recipe_backend.repository.UserSavedRepository;
import org.zhan.recipe_backend.service.RecipeCardService;
import org.zhan.recipe_backend.utils.AuthUtils;
import org.zhan.recipe_backend.utils.ConvertUtils;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class RecipeCardServiceImpl implements RecipeCardService {

    @Autowired
    private RecipeRepository recipeRepository;
    @Autowired
    private FlavourRepository flavourRepository;
    @Autowired
    private CuisineRepository cuisineRepository;

    @Autowired
    private UserSavedRepository userSavedRepository;

    public Slice<RecipeCardDto> getRecipeCards(RecipeCardDto cardParam,int page, int size) {

        Pageable pageable = PageRequest.of(page - 1, size);
        Slice<Recipe> recipeSlice = recipeRepository.searchRecipesByParams(cardParam, cardParam.getMinTime(), cardParam.getMaxTime(),pageable);

        Long userId = AuthUtils.getCurrentUserIdOrNull();

        return recipeSlice.map(recipe -> {
            // 先做纯粹的数据类型转换
            RecipeCardDto dto = ConvertUtils.convertToCardDto(recipe);

            // 如果用户已登录，在 Service 层判断是否收藏
            if (userId != null) {
                boolean isSaved = userSavedRepository.existsByUserIdAndRecipeId(userId, recipe.getId());
                dto.setIsLiked(isSaved);
            }
            return dto;
        });




    }


    @Override
    public List<String> getFlavours() {
        List<Flavour> flavours = flavourRepository.findAll();
        return flavours.stream()
                .map(Flavour::getName)
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getCuisines() {
        List<Cuisine> cuisines = cuisineRepository.findAll();
        return cuisines.stream()
                .map(Cuisine::getName) // 只提取名字
                .collect(Collectors.toList());
    }

}
