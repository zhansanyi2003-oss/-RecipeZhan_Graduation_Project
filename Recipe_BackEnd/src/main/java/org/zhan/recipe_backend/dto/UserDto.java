package org.zhan.recipe_backend.dto;


import lombok.Data;
import org.zhan.recipe_backend.common.RoleEnum;

@Data
public class UserDto {

    private  String username;
    private  String  email;
    private  String  avatarUrl;
    private String   bio;
    private RoleEnum role;
    private Integer savedCount;
    private Integer createdCount;
}
