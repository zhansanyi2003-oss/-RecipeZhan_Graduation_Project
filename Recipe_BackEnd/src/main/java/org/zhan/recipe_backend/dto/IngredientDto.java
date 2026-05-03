package org.zhan.recipe_backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class IngredientDto {

    @NotBlank(message = "Ingredient name is required")
    private String name ;
    private String amount;
    private  String note;
}
