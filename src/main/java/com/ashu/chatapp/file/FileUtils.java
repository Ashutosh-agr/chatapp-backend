package com.ashu.chatapp.file;

import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class FileUtils {

    private FileUtils() {}

    public static byte[] readFileFromLocation(String filePath) {
        if (filePath == null || filePath.isEmpty()) return new byte[0];

        try {
            Path path = new File(filePath).toPath();
            return Files.readAllBytes(path);
        } catch (IOException e) {
            log.warn("Could not read file at {}", filePath);
            return new byte[0];
        }
    }
}
