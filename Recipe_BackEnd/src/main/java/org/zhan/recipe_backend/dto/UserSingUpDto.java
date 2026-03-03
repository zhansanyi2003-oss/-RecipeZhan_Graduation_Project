package org.zhan.recipe_backend.dto;


import lombok.Data;
@Data
public class UserSingUpDto {
    private String username;
    private String password;
    private String email;
    private String avatarUrl;
}
