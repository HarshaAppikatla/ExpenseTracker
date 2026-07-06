package com.expenseflow.core.storage.impl;

import com.expenseflow.core.config.StorageProperties;
import com.expenseflow.core.storage.StorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Slf4j
@Service
@RequiredArgsConstructor
public class LocalStorageService implements StorageService {

    private final StorageProperties storageProperties;
    private Path rootDir;

    @PostConstruct
    public void init() {
        try {
            this.rootDir = Paths.get(storageProperties.getReceiptsDir()).toAbsolutePath().normalize();
            Files.createDirectories(this.rootDir);
            log.info("Initialized local file storage root at: {}", this.rootDir);
        } catch (IOException e) {
            throw new IllegalStateException("Could not initialize local storage root directory", e);
        }
    }

    @Override
    public void store(String key, InputStream content, String contentType, long size) {
        try {
            Path targetFile = this.rootDir.resolve(key).normalize();
            if (!targetFile.startsWith(this.rootDir)) {
                throw new IllegalArgumentException("Cannot store file outside directory bounds: " + key);
            }
            Files.copy(content, targetFile, StandardCopyOption.REPLACE_EXISTING);
            log.info("Successfully stored local file key: {}", key);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write file: " + key, e);
        }
    }

    @Override
    public InputStream load(String key) {
        try {
            Path targetFile = this.rootDir.resolve(key).normalize();
            if (!targetFile.startsWith(this.rootDir)) {
                throw new IllegalArgumentException("Cannot read file outside directory bounds: " + key);
            }
            if (!Files.exists(targetFile)) {
                throw new IllegalStateException("File does not exist: " + key);
            }
            return Files.newInputStream(targetFile);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load file: " + key, e);
        }
    }

    @Override
    public void delete(String key) {
        try {
            Path targetFile = this.rootDir.resolve(key).normalize();
            if (targetFile.startsWith(this.rootDir)) {
                Files.deleteIfExists(targetFile);
                log.info("Successfully deleted local file key: {}", key);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to delete file: " + key, e);
        }
    }

    @Override
    public boolean exists(String key) {
        Path targetFile = this.rootDir.resolve(key).normalize();
        return targetFile.startsWith(this.rootDir) && Files.exists(targetFile);
    }
}
