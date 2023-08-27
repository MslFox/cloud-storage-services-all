package com.mslfox.cloudStorageServices.messages;

import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Setter
@ConfigurationProperties("error")
public class ErrorMessage {
    private String wrongPassword;
    private String userNotFound;
    private String gettingFile;
    private String gettingFileList;
    private String renameFile;
    private String uploadFile;
    private String deleteFile;
    private String internal;
    private String validation;
    private String securityInvalidUsername;
    private String jsonProcessing;
    private String jwtStartBearer;
    private String jwtBlacklisted;

    public String wrongPassword() {
        return wrongPassword;
    }

    public String userNotFound() {
        return userNotFound;
    }

    public String gettingFile() {
        return gettingFile;
    }

    public String gettingFileList() {
        return gettingFileList;
    }

    public String renameFile() {
        return renameFile;
    }

    public String uploadFile() {
        return uploadFile;
    }

    public String deleteFile() {
        return deleteFile;
    }

    public String internalError() {
        return internal;
    }

    public String validation() {
        return validation;
    }

    public String getSecurityInvalidUsernameMessage() {
        return securityInvalidUsername;
    }

    public String jsonProcessing() {
        return jsonProcessing;
    }
    public String jwtStartBearer() {
        return jwtStartBearer;
    }
    public String getJwtBlacklisted() {
        return jwtBlacklisted;
    }
}
