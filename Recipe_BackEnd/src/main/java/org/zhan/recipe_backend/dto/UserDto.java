package org.zhan.recipe_backend.dto;


import lombok.Data;

@Data
public class UserDto {

    private  String username;
    private  String  email;
    private  String  avatarUrl;
    private String   bio;
    private Integer savedCount;
    private Integer createdCount;
}
