package com.mslfox.cloudStorageServices.repository;

import com.mslfox.cloudStorageServices.model.file.FileInfoResponse;
import com.mslfox.cloudStorageServices.repository.file.FileSystem.FileSystemStorage;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static com.mslfox.cloudStorageServices.constant.TestConstantHolder.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class FileSystemStorageTest {

    private FileSystemStorage fileSystemStorage;

    @BeforeEach
    public void initStorage() throws IOException {
        System.out.println("TEST_STORAGE_LOCATION " + TEST_STORAGE_LOCATION);
        fileSystemStorage = new FileSystemStorage(TEST_STORAGE_LOCATION + "/");
    }

    @AfterEach
    public void deleteFileSystemStorage() throws IOException {
        FileUtils.forceDelete(new File(TEST_STORAGE_LOCATION));
    }

    @Test
    public void shouldStoreAndLoadAsResource() throws IOException {
        // Arrange
        fileSystemStorage.store(TEST_USERNAME, TEST_FILE_NAME, TEST_FILE_CONTENT.getBytes());
        // Act
        final var resource = fileSystemStorage.loadAsResource(TEST_USERNAME, TEST_FILE_NAME);
        // Assert
        assertEquals(TEST_FILE_CONTENT, new String(Files.readAllBytes(resource.getFile().toPath())));
    }

    @Test
    public void shouldStoreAndDeleteFile() throws IOException{
        // Arrange
        fileSystemStorage.store(TEST_USERNAME, TEST_FILE_NAME, TEST_FILE_CONTENT.getBytes());
        // Act
        fileSystemStorage.delete(TEST_USERNAME, TEST_FILE_NAME);
        // Assert
        final var files = fileSystemStorage.loadFileInfoList(TEST_USERNAME, 3);
        assertTrue(files.isEmpty());
    }

    @Test
    public void shouldStoreAndUpdateFileName() throws IOException {
        // Arrange
        fileSystemStorage.store(TEST_USERNAME, TEST_FILE_NAME, TEST_FILE_CONTENT.getBytes());
        // Act
        fileSystemStorage.updateFile(TEST_USERNAME, TEST_FILE_NAME, TEST_FILE_RENAMED_NAME);
        // Assert
        final var files = fileSystemStorage.loadFileInfoList(TEST_USERNAME, 10);
        assertEquals(1, files.size());
        assertEquals(TEST_FILE_RENAMED_NAME, files.get(0).getFilename());
    }

    @Test
    public void shouldLoadFileInfoList() throws IOException, InterruptedException {
        // Arrange
        final var testFilename1 = 1 + TEST_FILE_NAME;
        final var testFilename2 = 2 + TEST_FILE_NAME;
        fileSystemStorage.store(TEST_USERNAME, testFilename1, TEST_FILE_CONTENT.getBytes());
        Thread.sleep(10); // necessary for correct file creation date. if test fails with ordered List, try to up sleep time
        fileSystemStorage.store(TEST_USERNAME, testFilename2, TEST_FILE_CONTENT.getBytes());
        // Act
        final List<FileInfoResponse> files = fileSystemStorage.loadFileInfoList(TEST_USERNAME, 10);
        // Assert
        assertEquals(2, files.size());
        //List ordered by creation date
        assertEquals(testFilename2, files.get(0).getFilename());
        assertEquals(testFilename1, files.get(1).getFilename());
    }

    @Test
    @DisplayName("should throw Exception all public methods of FileStorage.class")
    public void shouldThrowException() {
        // userName = null
        assertThrows(NullPointerException.class, () -> fileSystemStorage.loadFileInfoList(null, 3));
        // no such file
        assertThrows(IOException.class, () -> fileSystemStorage.updateFile(TEST_USERNAME, TEST_FILE_NAME, TEST_FILE_RENAMED_NAME));
        assertThrows(IOException.class, () -> fileSystemStorage.delete(TEST_USERNAME, TEST_FILE_NAME));
        assertThrows(IOException.class, () -> fileSystemStorage.loadAsResource(TEST_USERNAME, TEST_FILE_NAME));
        // byte[] == null
        assertThrows(IOException.class, () -> fileSystemStorage.store(TEST_USERNAME, TEST_FILE_NAME, null));
        // filename == null
        assertThrows(IOException.class, () -> fileSystemStorage.store(TEST_USERNAME, null, TEST_FILE_CONTENT.getBytes()));
    }
}
