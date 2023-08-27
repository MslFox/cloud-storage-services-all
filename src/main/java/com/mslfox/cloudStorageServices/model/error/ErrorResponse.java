package com.mslfox.cloudStorageServices.model.error;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "error", description = "Represent an error that may occur when processing request")
public class ErrorResponse {
    private String message;
    private long id;
}
