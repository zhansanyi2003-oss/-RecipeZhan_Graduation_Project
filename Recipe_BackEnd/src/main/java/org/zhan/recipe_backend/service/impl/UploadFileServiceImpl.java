package org.zhan.recipe_backend.service.impl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.zhan.recipe_backend.service.UploadFileService;

import java.io.File;
import java.io.IOException;
@Service
public class UploadFileServiceImpl  implements UploadFileService {

    @Value("${recipe.file.upload-dir}")
    private String uploadDir;

    @Value("${recipe.file.upload-url}")
    private String URL_PREFIX;
    @Override
    public String saveFile(MultipartFile file) throws IOException {
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String md5 = DigestUtils.md5DigestAsHex(file.getInputStream());
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String newFileName = md5 + extension;
        File dest = new File(uploadDir + newFileName);
        if (!dest.exists()) {
            file.transferTo(dest);
        }
        return URL_PREFIX + newFileName;

    }
}
