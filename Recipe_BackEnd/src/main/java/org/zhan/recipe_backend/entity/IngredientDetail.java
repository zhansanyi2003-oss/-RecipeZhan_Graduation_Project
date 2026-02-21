package org.zhan.recipe_backend.entity;

import lombok.Data;
import java.io.Serializable;

@Data
public class IngredientDetail implements Serializable {
    private String name;
    private String amount;
    private String note;
}