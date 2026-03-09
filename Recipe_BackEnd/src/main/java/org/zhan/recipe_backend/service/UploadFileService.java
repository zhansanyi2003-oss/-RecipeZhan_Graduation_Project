package org.zhan.recipe_backend.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UploadFileService {
    String saveFile(MultipartFile file) throws IOException;
}
