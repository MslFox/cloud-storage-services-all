package com.mslfox.cloudStorageServices.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Objects;
import java.util.stream.Stream;

@OpenAPIDefinition(info = @Info(title = "Authentication API and Storage Files Manager API",
        description = "MSL Group production",
        version = "1.0.0"))

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenApiCustomiser openApiCustomiser() {
        return openApi -> openApi.getPaths()
                .values()
                .stream()
                .flatMap(pathItem -> Stream.of(
                        pathItem.getGet(),
                        pathItem.getPut(),
                        pathItem.getPost(),
                        pathItem.getDelete(),
                        pathItem.getOptions(),
                        pathItem.getHead(),
                        pathItem.getPatch(),
                        pathItem.getTrace()))
                .filter(Objects::nonNull)
                .forEach(operation -> operation.setOperationId(null));
    }
}