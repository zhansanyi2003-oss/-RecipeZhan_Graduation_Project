package org.zhan.recipe_backend.controller;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.DigestUtils;
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
    @Value("${recipe.file.upload-dir}")
    private String uploadDir;;

    // URL 前缀不变，依然是映射到 localhost
    @Value("${recipe.file.upload-url}")
    private String URL_PREFIX;

    @PostMapping("/upload")
    public Result uploadImage(MultipartFile file) {
        try {
            // 1. 确保目录存在
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // 2. MD5 极速秒传逻辑 (和之前一样)
            String md5 = DigestUtils.md5DigestAsHex(file.getInputStream());
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFileName = md5 + extension;

            File dest = new File(uploadDir + newFileName);
            if (!dest.exists()) {
                file.transferTo(dest); // 不存在才保存
            }

            // 3. 返回前端可访问的 URL
            String imageUrl = URL_PREFIX + newFileName;

            return Result.Success(imageUrl);

        } catch (IOException e) {
            e.printStackTrace();
            return Result.Error("文件上传失败");
        }
    }
}