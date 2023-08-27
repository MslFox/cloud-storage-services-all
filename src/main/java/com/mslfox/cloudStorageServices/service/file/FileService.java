package com.mslfox.cloudStorageServices.service.file;

import org.springframework.core.io.Resource;

import java.util.List;

public interface FileService<FileInfoResponse, FileRequest, FileRenameRequest, FileUploadRequest> {
    String upload(FileUploadRequest fileUploadRequest) throws RuntimeException;

    List<FileInfoResponse> getFileInfoList(int limit);

    String deleteFile(FileRequest fileRequest) throws RuntimeException;

    Resource getFileResource(FileRequest fileRequest);

    String renameFile(FileRenameRequest fileRenameRequest) throws RuntimeException;
}
