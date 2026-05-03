package org.zhan.recipe_backend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class RecipeStepDto {
    @NotBlank(message = "Step content is required")
    private String content;
    private List<String> imageUrls;
}
