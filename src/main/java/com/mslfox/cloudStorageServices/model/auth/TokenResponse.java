package com.mslfox.cloudStorageServices.model.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mslfox.cloudStorageServices.constant.HeaderNameHolder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "auth-token", description = "Contains json web token")
public class TokenResponse {
    @JsonProperty(HeaderNameHolder.TOKEN_HEADER_NAME)
    private String authToken;
}


