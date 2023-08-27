package com.mslfox.cloudStorageServices.controller.openApi;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Controller
public class OpenApiController {
    @GetMapping("/swagger-ui")
    public RedirectView redirectToSwaggerUI() {
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("/swagger-ui/auth.html");
        return redirectView;
    }

    @GetMapping("/openapi.yaml")
    public ResponseEntity<String> getOpenApiYaml() throws IOException {
        try (FileInputStream fis = new FileInputStream("openapi.yaml")) {
            String yamlContent = new String(fis.readAllBytes(), StandardCharsets.UTF_8);
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_PLAIN)
                    .body(yamlContent);
        }
    }
}