package org.zhan.recipe_backend.dto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class UserPreferenceDto {
    @Size(max = 5, message = "dietary can contain at most 5 items")
    private List<@NotBlank(message = "dietary item cannot be blank") @Size(max = 50, message = "dietary item is too long") String> dietary = new ArrayList<>();

    @Size(max = 20, message = "allergies can contain at most 20 items")
    private List<@NotBlank(message = "allergy item cannot be blank") @Size(max = 50, message = "allergy item is too long") String> allergies = new ArrayList<>();

    @Pattern(regexp = "^(|Beginner|Intermediate|Master)$", message = "skillLevel must be Beginner, Intermediate or Master")
    private String skillLevel;

    @Pattern(regexp = "^(|15|30|60|999)$", message = "timeAvailability must be 15, 30, 60 or 999")
    private String timeAvailability;

    @Size(max = 3, message = "flavours can contain at most 3 items")
    private List<@NotBlank(message = "flavour item cannot be blank") @Size(max = 50, message = "flavour item is too long") String> flavours = new ArrayList<>();

    @Size(max = 3, message = "cuisines can contain at most 3 items")
    private List<@NotBlank(message = "cuisine item cannot be blank") @Size(max = 50, message = "cuisine item is too long") String> cuisines = new ArrayList<>();

    @Size(max = 3, message = "ingredients can contain at most 3 items")
    private List<@NotBlank(message = "ingredient item cannot be blank") @Size(max = 80, message = "ingredient item is too long") String> ingredients = new ArrayList<>();

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
