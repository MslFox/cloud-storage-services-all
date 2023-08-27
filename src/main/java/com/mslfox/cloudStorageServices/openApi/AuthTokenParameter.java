package com.mslfox.cloudStorageServices.openApi;
import com.mslfox.cloudStorageServices.constant.HeaderNameHolder;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Parameter(in = ParameterIn.HEADER, name = HeaderNameHolder.TOKEN_HEADER_NAME, schema = @Schema(name = "string"), description = "Unique JWT access token")
public @interface AuthTokenParameter {
}