package com.mslfox.cloudStorageServices.entities.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;

@RequiredArgsConstructor
public enum UserAuthority implements GrantedAuthority {
    ROLE_USER("ROLE_USER"),
    ROLE_ADMIN("ROLE_ADMIN");
    private final String value;

    @Override
    public String getAuthority() {
        return value;
    }
}
