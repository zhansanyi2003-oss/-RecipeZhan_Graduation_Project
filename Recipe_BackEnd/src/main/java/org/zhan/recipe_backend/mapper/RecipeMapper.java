package org.zhan.recipe_backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.zhan.recipe_backend.document.RecipeDoc;
import org.zhan.recipe_backend.dto.IngredientDto;
import org.zhan.recipe_backend.dto.RecipeCardDto;
import org.zhan.recipe_backend.dto.RecipeDetailDto;
import org.zhan.recipe_backend.entity.*;

@Mapper(componentModel = "spring", uses = UserMapper.class)
public interface RecipeMapper {


    @Mapping(source = "recipeFlavours", target = "flavours")
    @Mapping(source = "recipeCourses", target = "courses")
    @Mapping(source = "recipeCuisines", target = "cuisines")
    @Mapping(source = "recipeIngredients", target = "ingredients")
    @Mapping(source = "recipeDietTypes", target = "dietTypes")
    RecipeDoc toRecipeDoc(Recipe recipe);
    @Mapping(source = "recipeFlavours", target = "flavours")
    @Mapping(source = "recipeCourses", target = "courses")
    @Mapping(source = "recipeCuisines", target = "cuisines")
    @Mapping(source = "recipeDietTypes", target = "dietTypes")
    @Mapping(source = "recipeIngredients", target = "ingredients")
    @Mapping(target = "isLiked", ignore = true)
    @Mapping(target = "userScore", ignore = true)
    RecipeDetailDto toDetailDto(Recipe recipe);

    @Mapping(source = "recipeFlavours", target = "flavours")
    @Mapping(source = "recipeCourses", target = "courses")
    @Mapping(source = "recipeCuisines", target = "cuisines")
    @Mapping(source = "recipeDietTypes", target = "dietTypes")
    @Mapping(source = "recipeIngredients", target = "ingredientTags")
    @Mapping(target = "isLiked", ignore = true)
    RecipeCardDto toCardDto(Recipe recipe);

    @Mapping(source = "ingredient.name", target = "name")
    IngredientDto toIngredientDto(Recipe_Ingredient ri);

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

    default String mapDietType (Recipe_DietType rd) {
        if (rd == null || rd.getDietType() == null) return null;
        return rd.getDietType().getName();
    }

    default String mapIngredientToString(Recipe_Ingredient ri) {
        if (ri == null || ri.getIngredient() == null) return null;
        return ri.getIngredient().getName();
    }
    @Mapping(source = "ingredients", target = "ingredientTags")
    @Mapping(target = "isLiked", ignore = true)
    RecipeCardDto docToCardDto(RecipeDoc doc);
}