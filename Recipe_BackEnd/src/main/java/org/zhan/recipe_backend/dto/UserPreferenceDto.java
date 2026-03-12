package org.zhan.recipe_backend.dto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserPreferenceDto {
    private List<String> dietary = new ArrayList<>();
    private List<String> allergies = new ArrayList<>();
    private String skillLevel;
    private String timeAvailability;
    private List<String> flavours = new ArrayList<>();
    private List<String> cuisines = new ArrayList<>();
    private List<String> ingredients = new ArrayList<>();

    @JsonIgnore
    public Boolean isEmpty()
    {
        boolean noSkill = (skillLevel == null || skillLevel.trim().isEmpty());
        boolean noTime = (timeAvailability == null || timeAvailability.trim().isEmpty());
        return dietary.isEmpty() &&
                allergies.isEmpty() &&
                flavours.isEmpty() &&
                cuisines.isEmpty() &&
                ingredients.isEmpty() &&
                noSkill &&
                noTime;
    }

}