package com.mslfox.cloudStorageServices.service.auth;

import org.springframework.security.core.userdetails.UserDetailsService;

public interface AuthTokenService<Token, AuthData> extends UserDetailsService {
    Token login(AuthData authData);
}
