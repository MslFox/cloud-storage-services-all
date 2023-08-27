package com.mslfox.cloudStorageServices.dto.file;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "filename", description = "Contains non empty filename")
public class FileRequest {
    @NotEmpty
    @NotBlank
    private String filename;
}
