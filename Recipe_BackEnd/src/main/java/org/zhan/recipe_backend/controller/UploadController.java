package org.zhan.recipe_backend.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.zhan.recipe_backend.common.Result;
import org.zhan.recipe_backend.service.UploadFileService;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class UploadController {
    @Autowired
    private UploadFileService uploadFileService;
    @PostMapping("/upload")
    public Result uploadImage(MultipartFile file) {
        try {
            // 直接调用 Service 拿到 URL
            String imageUrl = uploadFileService.saveFile(file);
            return Result.Success(imageUrl);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.Error("文件上传失败");
        }

    }
}