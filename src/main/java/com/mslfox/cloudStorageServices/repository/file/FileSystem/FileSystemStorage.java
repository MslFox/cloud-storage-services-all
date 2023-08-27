package com.mslfox.cloudStorageServices.repository.file.FileSystem;

import com.mslfox.cloudStorageServices.model.file.FileInfoResponse;
import com.mslfox.cloudStorageServices.repository.file.FileStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Repository
public class FileSystemStorage implements FileStorage {
    private final Path storagePath;

    public FileSystemStorage(@Value("${file.system.storage.location:uploads/}") String storageLocation) throws IOException {
        this.storagePath = initFolder(Path.of(storageLocation));
        log.info("File storage initialized");
        log.info("File storage type: Local Filesystem");
        log.info("File storage location: {}", storagePath.toUri());
    }

    @Override
    public List<FileInfoResponse> loadFileInfoList(String username, int limit) throws IOException {
        Path userFolderPath = initFolder(storagePath.resolve(username));
        return Arrays.stream(Objects.requireNonNull(userFolderPath.toFile().listFiles()))
                .sorted(Comparator.comparingLong(File::lastModified).reversed())
                .limit(limit)
                .map(file -> {
                    try {
                        BasicFileAttributes attributes = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
                        var formatter = new SimpleDateFormat("yyy-MM-dd");
                        String creationDate = formatter.format(new Date(file.lastModified()));
                        return new FileInfoResponse(file.getName(), file.length(), creationDate);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
    }


    @Override
    public void store(String username, String filename, byte[] bytes) throws IOException {
        if (filename != null && bytes != null) {
            var userFolder = initFolder(storagePath.resolve(username));
            final var filePath = userFolder.resolve(filename);
            Files.write(filePath, bytes);
            log.info("User '{}' save file '{}'",
                    username,
                    filename);
        } else {
            throw new IOException();
        }
    }

    @Override
    public void delete(String username, String filename) throws IOException {
        final var filePath = getFilePath(username, filename);
        Files.delete(filePath);
        log.info("User '{}' delete file '{}'",
                username,
                filename);
    }

    @Override
    public Resource loadAsResource(String username, String filename) throws IOException {
        final var filePath = getFilePath(username, filename);
        return new UrlResource(filePath.toUri());
    }

    @Override
    public void updateFile(String username, String toUpdateFilename, String newFileName) throws IOException {
        final var toUpdateFilePath = getFilePath(username, toUpdateFilename);
        final var newFilePath = getIfExistUserFolderPath(username).resolve(newFileName);
        Files.move(toUpdateFilePath, newFilePath);
        log.info("User '{}' change filename '{}' to '{}'",
                username,
                toUpdateFilename,
                newFileName);
    }

    private Path initFolder(Path folderName) throws IOException {
        return Files.createDirectories(folderName);
    }

    private Path getIfExistUserFolderPath(String username) throws IOException {
        final var userFolderPath = storagePath.resolve(username);
        if (!Files.exists(userFolderPath)) throw new IOException();
        return userFolderPath;
    }

    private Path getFilePath(String username, String filename) throws IOException {
        final var filePath = getIfExistUserFolderPath(username).resolve(filename);
        if (!Files.exists(filePath)) throw new IOException();
        return filePath;
    }
}
