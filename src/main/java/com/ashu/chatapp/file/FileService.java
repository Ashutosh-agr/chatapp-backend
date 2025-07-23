package com.ashu.chatapp.file;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
public class FileService {

    @Value("${application.file.uploads.media-output-path}")
    private String fileUploadPath;

    @Value("${application.file.base-url}") 
    private String baseUrl;

    public SavedFile saveFile(@NonNull MultipartFile sourceFile, @NonNull String userId) {
        String fileExtension = getFileExtension(sourceFile.getOriginalFilename());
        String fileName = System.currentTimeMillis() + fileExtension;
        Path fullPath = Paths.get(fileUploadPath, "users", userId, fileName);

        try {
            Files.createDirectories(fullPath.getParent());
            Files.write(fullPath, sourceFile.getBytes());

            String fileUrl = baseUrl + "/uploads/users/" + userId + "/" + fileName;

            log.info("File saved to path: {}", fileUrl);
            return new SavedFile(fullPath.toString(), fileUrl);
        } catch (IOException e) {
            log.error("Failed to save file", e);
            return null;
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) return "";
        int lastDot = fileName.lastIndexOf('.');
        return (lastDot == -1) ? "" : fileName.substring(lastDot).toLowerCase();
    }
}
