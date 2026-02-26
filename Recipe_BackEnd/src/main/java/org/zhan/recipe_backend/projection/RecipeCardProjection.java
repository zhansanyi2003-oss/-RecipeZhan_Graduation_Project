package org.zhan.recipe_backend.projection;

import org.zhan.recipe_backend.common.DiffEnum;

import java.util.List;

public interface RecipeCardProjection {
    Long getId();
    String getTitle();
    String getCoverImage();
    DiffEnum getDiff();
    String getCookingTimeMin();
    List<String> getCuisines();
    List<String> getFlavours();
    List<String> getIngredientTags();
    Integer getLikesCount();


}