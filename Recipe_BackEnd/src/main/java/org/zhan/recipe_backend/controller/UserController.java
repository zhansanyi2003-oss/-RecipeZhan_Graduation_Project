package org.zhan.recipe_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.zhan.recipe_backend.common.Result;
import org.zhan.recipe_backend.dto.UserPreferenceDto;
import org.zhan.recipe_backend.service.UploadFileService;
import org.zhan.recipe_backend.service.UserService;
import org.zhan.recipe_backend.service.impl.UserServiceImpl;
import org.zhan.recipe_backend.utils.AuthUtils;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private UploadFileService uploadFileService;
    @GetMapping("/myRecipe")
    public Result getMyRecipe(@RequestParam(defaultValue = "1") Integer page,
                              @RequestParam(defaultValue = "12") Integer pageSize) {

        return  Result.Success(userService.getMyRecipe(page,pageSize));

    }
    @PostMapping("/save/{id}")
    public Result sumbmitSavedRecipe(@PathVariable long id,@RequestBody SaveRequest request) {


        return Result.Success( userService.toggleSaveRecipe(id,request.status));
    }

    @GetMapping("/saved")
    public Result getSavedRecipe(@RequestParam(defaultValue = "1") Integer page,
                                 @RequestParam(defaultValue = "12") Integer pageSize) {
        return Result.Success(userService.getSavedRecipe(page,pageSize));
    }
    private static class SaveRequest {
        public Boolean status;
    }
    @GetMapping()
    public Result getUserInfo()
    {
        return Result.Success(userService.getUserById());
    }

    @PostMapping("/avatar")
    public Result updateAvatar(@RequestParam("file") MultipartFile file) {
        try {
            // 1. 调用公共服务，把图片物理存盘，拿到 URL
            String newAvatarUrl = uploadFileService.saveFile(file);
            userService.updateAvatar( newAvatarUrl);

            // 3. 把全新的 URL 返回给前端展示
            return Result.Success(newAvatarUrl);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.Error("头像更新失败：" + e.getMessage());
        }
    }


    @PostMapping("/preference")
    public Result updatePreferences(@RequestBody UserPreferenceDto preferences) {
        userService.updateUserPreferences(preferences);
        return Result.Success("Preferences updated successfully");
    }

    @GetMapping("/preference")
    public Result getPreferences() {
        return Result.Success(userService.getUserPreferences());
    }

    @DeleteMapping("/avatar")
    public Result deleteAvatar() {
        userService.deleteAvatar();
         return  Result.Success();

    }


}
