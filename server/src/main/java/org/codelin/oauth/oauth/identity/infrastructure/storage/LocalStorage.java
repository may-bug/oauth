package org.codelin.oauth.oauth.identity.infrastructure.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * 本地文件存储实现
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalStorage implements FileStorage {

    @Value("${app.storage.local.base-path:./uploads}")
    private String basePath;

    @Override
    public String upload(String path, InputStream inputStream, String contentType) {
        try {
            Path filePath = Paths.get(basePath, path);
            Files.createDirectories(filePath.getParent());
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
            log.debug("File uploaded to: {}", filePath);
            return getUrl(path);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file: " + path, e);
        }
    }

    @Override
    public InputStream download(String path) {
        try {
            Path filePath = Paths.get(basePath, path);
            if (!Files.exists(filePath)) {
                throw new FileNotFoundException("File not found: " + path);
            }
            return Files.newInputStream(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to download file: " + path, e);
        }
    }

    @Override
    public void delete(String path) {
        try {
            Path filePath = Paths.get(basePath, path);
            Files.deleteIfExists(filePath);
            log.debug("File deleted: {}", filePath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + path, e);
        }
    }

    @Override
    public String getUrl(String path) {
        return "/files/" + path;
    }

    @Override
    public boolean exists(String path) {
        Path filePath = Paths.get(basePath, path);
        return Files.exists(filePath);
    }
}
