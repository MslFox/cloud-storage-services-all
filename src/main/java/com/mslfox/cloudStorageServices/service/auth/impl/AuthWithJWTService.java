package com.mslfox.cloudStorageServices.service.auth.impl;

import com.mslfox.cloudStorageServices.dto.auth.AuthRequest;
import com.mslfox.cloudStorageServices.entities.auth.UserEntity;
import com.mslfox.cloudStorageServices.exception.BadRequestException;
import com.mslfox.cloudStorageServices.messages.ErrorMessage;
import com.mslfox.cloudStorageServices.model.auth.TokenResponse;
import com.mslfox.cloudStorageServices.repository.auth.UserEntityRepository;
import com.mslfox.cloudStorageServices.security.JwtProvider;
import com.mslfox.cloudStorageServices.service.auth.AuthTokenService;
import com.mslfox.cloudStorageServices.util.Base64Util;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
@Data
public class AuthWithJWTService implements AuthTokenService<TokenResponse, AuthRequest> {
    private final UserEntityRepository userEntityRepository;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final ErrorMessage errorMessage;

    @Override
    public TokenResponse login(AuthRequest authRequest) throws RuntimeException {
        UserEntity userEntity;
        try{
            userEntity = loadUserByUsername(getUsernameEncoded(authRequest.getUsername()));
            final var userEncodedPassword = userEntity.getPassword();
            final var authRequestPassword = authRequest.getPassword();
            if (passwordEncoder.matches(authRequestPassword, userEncodedPassword)) {
                final var jwt = jwtProvider.generateJwt(userEntity);
                return new TokenResponse(jwt);
            } else {
                throw new BadRequestException(errorMessage.wrongPassword());
            }
        } catch (UsernameNotFoundException e){
            userEntity = new UserEntity(
                    getUsernameEncoded(authRequest.getUsername()),
                    passwordEncoder.encode(authRequest.getPassword()));
            userEntityRepository.save(userEntity);
            final var jwt = jwtProvider.generateJwt(userEntity);
            return new TokenResponse(jwt);
        }
    }

    @Override
    public UserEntity loadUserByUsername(String username) throws UsernameNotFoundException {
        return userEntityRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(errorMessage.userNotFound()));
    }

    private String getUsernameEncoded(String originalString) {
        return Base64Util.encode(originalString);
    }

}
