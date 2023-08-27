package com.mslfox.cloudStorageServices.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mslfox.cloudStorageServices.constant.HeaderNameHolder;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "credentials", description = "login: Contains non-empty login validated by the @Email constraint. <br>" +
        "password:  must be at least 6 characters and contain at least one digit and one uppercase letter ")
public class AuthRequest {
    @Email
    @NotEmpty
    @JsonProperty(HeaderNameHolder.USER_HEADER_NAME)
    private String username;
    @Size(min = 6, max = 64, message = "password:  must be at least 6 characters and contain at least one digit and one uppercase letter")
    @NotEmpty
    @Pattern(regexp = "^(?=.*\\d)(?=.*[A-Z]).{6,}$", message = "password:  must be at least 6 characters and contain at least one digit and one uppercase letter")
    private String password;
}

