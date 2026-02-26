package org.zhan.recipe_backend.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class RecipeStep implements Serializable {

        private String content;
        private List<String>  imageUrls;
}

