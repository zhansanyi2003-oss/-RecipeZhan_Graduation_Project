package org.zhan.recipe_backend.dto;

import lombok.Data;
import org.zhan.recipe_backend.common.RoleEnum;

@Data
public class UserLoginDto {
    private String username;
    private  String password;
}
