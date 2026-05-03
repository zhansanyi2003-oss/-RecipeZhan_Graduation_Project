package org.zhan.recipe_backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.zhan.recipe_backend.common.Result;
import org.zhan.recipe_backend.service.UploadFileService;

import java.io.IOException;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
public class UploadController {

    @Autowired
    private UploadFileService uploadFileService;

    @PostMapping("/upload")
    public Result uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        String imageUrl = uploadFileService.saveFile(file);
        return Result.Success(imageUrl);
    }
}
