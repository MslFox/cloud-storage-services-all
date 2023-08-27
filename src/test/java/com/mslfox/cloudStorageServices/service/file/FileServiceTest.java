package com.mslfox.cloudStorageServices.service.file;

import com.mslfox.cloudStorageServices.dto.file.FileRenameRequest;
import com.mslfox.cloudStorageServices.dto.file.FileRequest;
import com.mslfox.cloudStorageServices.dto.file.FileUploadRequest;
import com.mslfox.cloudStorageServices.exception.InternalServerException;
import com.mslfox.cloudStorageServices.messages.ErrorMessage;
import com.mslfox.cloudStorageServices.messages.SuccessMessage;
import com.mslfox.cloudStorageServices.model.file.FileInfoResponse;
import com.mslfox.cloudStorageServices.repository.file.FileSystem.FileSystemStorage;
import com.mslfox.cloudStorageServices.service.file.impl.FileServiceImpl;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.mslfox.cloudStorageServices.constant.TestConstantHolder.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FileServiceTest {

    @Mock
    private static ErrorMessage errorMessage;
    @Mock
    private static SuccessMessage successMessage;
    @Mock
    private FileSystemStorage fileSystemStorage;
    @InjectMocks
    private FileServiceImpl fileService;
    @Mock
    private Resource mockResource;
    @Mock
    private MultipartFile mockMultipartFile;
    private final static List<FileInfoResponse> testList = new ArrayList<>();
    private static int limit;
    private final static FileRequest testFileRequest = new FileRequest();
    private final static FileRenameRequest testFileRenameRequest = new FileRenameRequest();


    @BeforeAll
    public static void setup() {
        final var auth = new UsernamePasswordAuthenticationToken(TEST_USERNAME_ENCODED, TEST_PASSWORD_ENCODED);
        SecurityContextHolder.getContext().setAuthentication(auth);
        testList.add(mock(FileInfoResponse.class));
        limit = testList.size();
        testFileRequest.setFilename(TEST_FILE_NAME);
        testFileRenameRequest.setNewFilename(TEST_FILE_RENAMED_NAME);
        testFileRenameRequest.setToUpdateFilename(TEST_FILE_NAME);
    }

    @Test
    public void uploadSuccess() throws IOException {
        // Arrange
        final var fileUploadRequest = new FileUploadRequest(TEST_FILE_NAME, mockMultipartFile);
        when(successMessage.getUploadMessage()).thenReturn(TEST_SUCCESS_MESSAGE);
        final var bytes = TEST_FILE_CONTENT.getBytes();
        when(mockMultipartFile.getBytes()).thenReturn(bytes);
        doNothing().when(fileSystemStorage).store(TEST_USERNAME_ENCODED, TEST_FILE_NAME, bytes);
        // Act
        final var result = fileService.upload(fileUploadRequest);
        // Assert
        verify(fileSystemStorage, times(1)).store(TEST_USERNAME_ENCODED, TEST_FILE_NAME, mockMultipartFile.getBytes());
        verify(successMessage, times(1)).getUploadMessage();
        verify(errorMessage, times(0)).uploadFile();
        verifyNoMoreInteractions(fileSystemStorage, successMessage, errorMessage);
        assertEquals(TEST_SUCCESS_MESSAGE, result);
    }

    @Test
    public void uploadWithException() throws IOException {
        // Arrange
        final var fileUploadRequest = new FileUploadRequest(TEST_FILE_NAME, mockMultipartFile);
        when(errorMessage.uploadFile()).thenReturn(TEST_EXCEPTION_MESSAGE);
        final var bytes = TEST_FILE_CONTENT.getBytes();
        when(mockMultipartFile.getBytes()).thenReturn(bytes);
        doThrow(new IOException(TEST_EXCEPTION_MESSAGE))
                .when(fileSystemStorage).store(TEST_USERNAME_ENCODED, TEST_FILE_NAME, bytes);
        // Assert
        assertThrows(InternalServerException.class, () -> fileService.upload(fileUploadRequest));
        verify(fileSystemStorage, times(1)).store(TEST_USERNAME_ENCODED, TEST_FILE_NAME, mockMultipartFile.getBytes());
        verify(successMessage, times(0)).getUploadMessage();
        verify(errorMessage, times(1)).uploadFile();
        verifyNoMoreInteractions(fileSystemStorage, successMessage, errorMessage);
    }

    @Test
    public void getFileInfoListSuccess() throws IOException {
        // Arrange
        when(fileSystemStorage.loadFileInfoList(TEST_USERNAME_ENCODED, limit)).thenReturn(testList);
        // Act
        final var result = fileService.getFileInfoList(limit);
        verify(fileSystemStorage, times(1)).loadFileInfoList(TEST_USERNAME_ENCODED, limit);
        verify(errorMessage, times(0)).gettingFileList();
        verifyNoMoreInteractions(fileSystemStorage, successMessage, errorMessage);
        assertEquals(result, testList);
    }

    @Test
    public void getFileInfoListWithException() throws IOException {
        // Arrange
        when(fileSystemStorage.loadFileInfoList(TEST_USERNAME_ENCODED, limit))
                .thenThrow(new IOException(TEST_EXCEPTION_MESSAGE));
        // Act
        assertThrows(InternalServerException.class, () -> fileService.getFileInfoList(limit));
        verify(fileSystemStorage, times(1)).loadFileInfoList(TEST_USERNAME_ENCODED, limit);
        verify(errorMessage, times(1)).gettingFileList();
        verifyNoMoreInteractions(fileSystemStorage, successMessage, successMessage);
    }

    @Test
    public void deleteFileSuccess() throws IOException {
        // Arrange
        when(successMessage.getDeleteMessage()).thenReturn(TEST_SUCCESS_MESSAGE);
        doNothing().when(fileSystemStorage).delete(TEST_USERNAME_ENCODED, TEST_FILE_NAME);
        // Act
        final var result = fileService.deleteFile(testFileRequest);
        // Assert
        verify(fileSystemStorage, times(1)).delete(TEST_USERNAME_ENCODED, TEST_FILE_NAME);
        verify(successMessage, times(1)).getDeleteMessage();
        verify(errorMessage, times(0)).deleteFile();
        verifyNoMoreInteractions(fileSystemStorage, successMessage, errorMessage);
        assertEquals(TEST_SUCCESS_MESSAGE, result);
    }

    @Test
    public void deleteFileWithException() throws IOException {
        // Arrange
        when(errorMessage.deleteFile()).thenReturn(TEST_EXCEPTION_MESSAGE);
        doThrow(new IOException(TEST_EXCEPTION_MESSAGE))
                .when(fileSystemStorage).delete(TEST_USERNAME_ENCODED, TEST_FILE_NAME);
        // Assert
        assertThrows(InternalServerException.class, () -> fileService.deleteFile(testFileRequest));
        verify(fileSystemStorage, times(1)).delete(TEST_USERNAME_ENCODED, TEST_FILE_NAME);
        verify(successMessage, times(0)).getDeleteMessage();
        verify(errorMessage, times(1)).deleteFile();
        verifyNoMoreInteractions(fileSystemStorage, successMessage, successMessage);
    }

    @Test
    public void getFileResourceSuccess() throws IOException {
        // Arrange
        when(fileSystemStorage.loadAsResource(TEST_USERNAME_ENCODED, TEST_FILE_NAME)).thenReturn(mockResource);
        // Act
        final var result = fileService.getFileResource(testFileRequest);
        // Assert
        verify(fileSystemStorage, times(1)).loadAsResource(TEST_USERNAME_ENCODED, TEST_FILE_NAME);
        verify(errorMessage, times(0)).gettingFile();
        verifyNoMoreInteractions(fileSystemStorage, successMessage, successMessage);
        assertEquals(mockResource, result);
    }

    @Test
    public void getFileResourceWithException() throws IOException {
        // Arrange
        when(fileSystemStorage.loadAsResource(TEST_USERNAME_ENCODED, TEST_FILE_NAME))
                .thenThrow(new IOException(TEST_EXCEPTION_MESSAGE));
        // Assert
        assertThrows(InternalServerException.class, () -> fileService.getFileResource(testFileRequest));
        verify(fileSystemStorage, times(1)).loadAsResource(TEST_USERNAME_ENCODED, TEST_FILE_NAME);
        verify(errorMessage, times(1)).gettingFile();
        verifyNoMoreInteractions(fileSystemStorage, successMessage, successMessage);
    }

    @Test
    public void renameFileSuccess() throws IOException {
        // Arrange
        when(successMessage.getRenameMessage()).thenReturn(TEST_SUCCESS_MESSAGE);
        doNothing().when(fileSystemStorage).updateFile(TEST_USERNAME_ENCODED, TEST_FILE_NAME, TEST_FILE_RENAMED_NAME);
        // Act
        final var result = fileService.renameFile(testFileRenameRequest);
        // Assert
        verify(fileSystemStorage, times(1)).updateFile(TEST_USERNAME_ENCODED, TEST_FILE_NAME, TEST_FILE_RENAMED_NAME);
        verify(successMessage, times(1)).getRenameMessage();
        verify(errorMessage, times(0)).renameFile();
        verifyNoMoreInteractions(fileSystemStorage, successMessage, errorMessage);
        assertEquals(TEST_SUCCESS_MESSAGE, result);
    }

    @Test
    public void renameFileWithException() throws IOException {
        // Arrange
        when(errorMessage.renameFile()).thenReturn(TEST_SUCCESS_MESSAGE);
        doThrow(new IOException(TEST_EXCEPTION_MESSAGE))
                .when(fileSystemStorage).updateFile(TEST_USERNAME_ENCODED, TEST_FILE_NAME, TEST_FILE_RENAMED_NAME);
        // Assert
        assertThrows(InternalServerException.class, () -> fileService.renameFile(testFileRenameRequest));
        verify(fileSystemStorage, times(1)).updateFile(TEST_USERNAME_ENCODED, TEST_FILE_NAME, TEST_FILE_RENAMED_NAME);
        verify(successMessage, times(0)).getRenameMessage();
        verify(errorMessage, times(1)).renameFile();
        verifyNoMoreInteractions(fileSystemStorage, successMessage, errorMessage);
    }
}
