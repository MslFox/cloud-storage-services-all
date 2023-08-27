package com.mslfox.cloudStorageServices.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "upload-file", description = "Contains non empty filename")
public class FileUploadRequest {
    @NotEmpty
    String filename;
    @NotNull
    MultipartFile file;

    public void setFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File content must not be empty");
        }
        this.file = file;
    }
}
