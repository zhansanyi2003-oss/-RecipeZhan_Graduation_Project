package org.zhan.recipe_backend.controller;


import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.zhan.recipe_backend.common.Result;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class UploadController {

    // 🏆 修改 1：动态获取项目根目录下的 /uploads 文件夹
    // System.getProperty("user.dir") 会自动找到你项目的根路径
    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    // URL 前缀不变，依然是映射到 localhost
    private static final String URL_PREFIX = "http://localhost:8888/images/";

    @PostMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.Error("Empty File");

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFileName = UUID.randomUUID().toString() + extension;

        // 🏆 修改 2：打印一下路径，方便你演示时给老师看（显得很专业）
        System.out.println("图片保存路径: " + UPLOAD_DIR + newFileName);

        File destDir = new File(UPLOAD_DIR);
        if (!destDir.exists()) {
            destDir.mkdirs(); // 自动创建 uploads 文件夹
        }

        try {
            File destFile = new File(destDir, newFileName);
            file.transferTo(destFile);
            return Result.Success(URL_PREFIX + newFileName);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.Error("Upload Failed");
        }
    }
}