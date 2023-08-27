package com.mslfox.cloudStorageServices.dto.file;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileRenameRequest {
    @NotEmpty
    private String newFilename;
    @NotEmpty
    private String toUpdateFilename;
}
