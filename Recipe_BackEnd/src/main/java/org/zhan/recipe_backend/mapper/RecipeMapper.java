package org.zhan.recipe_backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.zhan.recipe_backend.document.RecipeDoc;
import org.zhan.recipe_backend.dto.IngredientDto;
import org.zhan.recipe_backend.dto.RecipeCardDto;
import org.zhan.recipe_backend.dto.RecipeDetailDto;
import org.zhan.recipe_backend.entity.*;

@Mapper(componentModel = "spring")
public interface RecipeMapper {

    // ==========================================
    // 1. 卡片 DTO 转换 (给搜索列表页用)
    // ==========================================
    @Mapping(source = "recipeFlavours", target = "flavours")
    @Mapping(source = "recipeCourses", target = "courses")
    @Mapping(source = "recipeCuisines", target = "cuisines")
    @Mapping(source = "ingredientsList", target = "ingredientTags")
    // 🚨 架构师细节：忽略无法直接转换的字段，消除编译警告
    @Mapping(target = "isLiked", ignore = true)
    RecipeCardDto toCardDto(Recipe recipe);
    @Mapping(source = "recipeFlavours", target = "flavours")
    @Mapping(source = "recipeCourses", target = "courses")
    @Mapping(source = "recipeCuisines", target = "cuisines")
    // 注意：ingredientsList 和 steps 字段名两边一样，MapStruct 会自动去转！
    @Mapping(target = "isLiked", ignore = true)
    @Mapping(target = "userScore", ignore = true)
    RecipeDetailDto toDetailDto(Recipe recipe);


    @Mapping(source = "ingredient.name", target = "name")
    IngredientDto toIngredientDto(Recipe_Ingredient ri);

    @Mapping(target = "isLiked", ignore = true)
    RecipeCardDto docToCardDto(RecipeDoc doc);

    // ==========================================
    // 3. 字典表黑魔法提取区 (小对象 -> 纯字符串)
    // ==========================================

    default String mapFlavour(Recipe_Flavour rf) {
        if (rf == null || rf.getFlavour() == null) return null;
        return rf.getFlavour().getName();
    }

    default String mapCourse(Recipe_Course rc) {
        if (rc == null || rc.getCourse() == null) return null;
        return rc.getCourse().getName();
    }

    default String mapCuisine(Recipe_Cuisine rc) {
        if (rc == null || rc.getCuisine() == null) return null;
        return rc.getCuisine().getName();
    }

    default String mapIngredientToString(Recipe_Ingredient ri) {
        if (ri == null || ri.getIngredient() == null) return null;
        return ri.getIngredient().getName();
    }
}