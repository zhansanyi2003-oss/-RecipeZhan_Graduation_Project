package org.zhan.recipe_backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SaveRequestDto {

    @NotNull(message = "status is required")
    private  Boolean status;
}
