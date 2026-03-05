package org.zhan.recipe_backend.dto;

import lombok.Data;

@Data
public class IngredientDto {

    private String name ;
    private String amount;
    private  String note;
}
