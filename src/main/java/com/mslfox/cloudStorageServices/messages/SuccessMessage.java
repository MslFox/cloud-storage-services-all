package com.mslfox.cloudStorageServices.messages;

import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
@Setter
@ConfigurationProperties("success")
public class SuccessMessage {
    private String delete;
    private String rename;
    private String upload;

    public String getDeleteMessage() {
        return delete;
    }

    public String getRenameMessage() {
        return rename;
    }

    public String getUploadMessage() {
        return upload;
    }
}
