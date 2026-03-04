package org.zhan.recipe_backend.dto;

import lombok.Data;
import org.zhan.recipe_backend.common.DiffEnum;

import java.util.List;

@Data
public class RecipeCardDto {


    private Long id;
    private String title;
    private String coverImage;
    private DiffEnum difficulty;
    private String cookingTimeMin;
    private List<String> flavours;
    private List<String> cuisines;
    private List<String> ingredientTags;
    private List<String> courses;
    private Integer likesCount;
    private Double averageRating ;
    private Integer ratingCount;
    private  Boolean isLiked;



    public String getCoursesStr() {
        return (courses!= null && !courses.isEmpty()) ? String.join(",", courses) : null;
    }

    public String getCuisinesStr() {
        return (cuisines != null && !cuisines.isEmpty()) ? String.join(",", cuisines) : null;
    }

    public String getFlavoursStr() {
        return (flavours != null && !flavours.isEmpty()) ? String.join(",", flavours) : null;
    }

    public String getIngredientsStr() {
        return (ingredientTags != null && !ingredientTags.isEmpty()) ? String.join(",", ingredientTags) : null;
    }

    // 顺手把时间也处理了
    public Integer getMaxTime() {
        if (cookingTimeMin == null || cookingTimeMin.isEmpty() || "60+".equals(cookingTimeMin)) return null;
        return Integer.parseInt(cookingTimeMin);
    }

    public Integer getMinTime() {
        if ("60+".equals(cookingTimeMin)) return 60;
        return null;
    }


}
