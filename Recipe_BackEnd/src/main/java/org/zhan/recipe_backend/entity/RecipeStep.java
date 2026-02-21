package org.zhan.recipe_backend.entity;

import lombok.Data;

import java.io.Serializable;

@Data
public class RecipeStep implements Serializable {
        private Integer stepNum; // 步骤序号
        private String desc;     // 做法描述
        private String imgUrl;   // 步骤图(可选)
}

