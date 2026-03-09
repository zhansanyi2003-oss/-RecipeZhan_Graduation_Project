package org.zhan.recipe_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.zhan.recipe_backend.common.Result;
import org.zhan.recipe_backend.service.UploadFileService;
import org.zhan.recipe_backend.service.impl.UserServiceImpl;
import org.zhan.recipe_backend.utils.AuthUtils;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserServiceImpl userServiceImpl;

    @Autowired
    private UploadFileService uploadFileService;
    @GetMapping("/myRecipe")
    public Result getMyRecipe() {

        return  Result.Success(userServiceImpl.getMyRecipe());

    }
    @PostMapping("/save/{id}")
    public Result sumbmitSavedRecipe(@PathVariable long id,@RequestBody SaveRequest request) {


        return Result.Success( userServiceImpl.toggleSaveRecipe(id,request.status));
    }

    @GetMapping("/saved")
    public Result getSavedRecipe(@RequestParam(defaultValue = "1") Integer page,
                                 @RequestParam(defaultValue = "12") Integer pageSize) {
        return Result.Success(userServiceImpl.getSavedRecipe(page,pageSize));
    }
    private static class SaveRequest {
        public Boolean status;
    }
    @GetMapping()
    public Result getUserInfo()
    {
        return Result.Success(userServiceImpl.getUserById());
    }

    @PostMapping("/avatar")
    public Result updateAvatar(@RequestParam("file") MultipartFile file) {
        try {
            // 1. 调用公共服务，把图片物理存盘，拿到 URL
            String newAvatarUrl = uploadFileService.saveFile(file);
            userServiceImpl.updateAvatar( newAvatarUrl);

            // 3. 把全新的 URL 返回给前端展示
            return Result.Success(newAvatarUrl);

        } catch (Exception e) {
            e.printStackTrace();
            return Result.Error("头像更新失败：" + e.getMessage());
        }
    }

    @DeleteMapping("/avatar")
    public Result deleteAvatar() {
        userServiceImpl.deleteAvatar();
         return  Result.Success();

    }




}
