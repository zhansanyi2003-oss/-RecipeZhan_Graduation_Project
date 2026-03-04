package org.zhan.recipe_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.zhan.recipe_backend.common.Result;
import org.zhan.recipe_backend.service.impl.UserServiceImpl;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserServiceImpl userServiceImpl;
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



}
