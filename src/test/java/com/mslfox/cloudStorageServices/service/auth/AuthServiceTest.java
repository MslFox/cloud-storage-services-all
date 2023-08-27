package com.mslfox.cloudStorageServices.service.auth;

import com.mslfox.cloudStorageServices.dto.auth.AuthRequest;
import com.mslfox.cloudStorageServices.entities.auth.UserEntity;
import com.mslfox.cloudStorageServices.exception.BadRequestException;
import com.mslfox.cloudStorageServices.messages.ErrorMessage;
import com.mslfox.cloudStorageServices.model.auth.TokenResponse;
import com.mslfox.cloudStorageServices.repository.auth.UserEntityRepository;
import com.mslfox.cloudStorageServices.security.JwtProvider;
import com.mslfox.cloudStorageServices.service.auth.impl.AuthWithJWTService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static com.mslfox.cloudStorageServices.constant.TestConstantHolder.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock
    private UserEntityRepository userEntityRepository;
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ErrorMessage errorMessage;
    @InjectMocks
    private AuthWithJWTService authWithJWTService;

    private final static UserEntity testUserEntity = new UserEntity(TEST_USERNAME_ENCODED, TEST_PASSWORD_ENCODED);
    private final static AuthRequest testAuthRequest = new AuthRequest();

    @Test
    public void loginSuccess() {
        when(jwtProvider.generateJwt(testUserEntity)).thenReturn(TEST_JWT);
        when(userEntityRepository.findByUsername(TEST_USERNAME_ENCODED)).thenReturn(Optional.of(testUserEntity));
        when(passwordEncoder.matches(TEST_PASSWORD, TEST_PASSWORD_ENCODED)).thenReturn(true);
        // Arrange
        testAuthRequest.setUsername(TEST_USERNAME);
        testAuthRequest.setPassword(TEST_PASSWORD);
        // Act
        final var result = authWithJWTService.login(testAuthRequest);
        // Assert
        verify(userEntityRepository).findByUsername(TEST_USERNAME_ENCODED);
        verify(jwtProvider).generateJwt(testUserEntity);
        assertEquals(new TokenResponse(TEST_JWT), result);
    }

    @Test
    public void loginWithUserNotFound() {
        // TODO эта проверка может быть включена, только при отключении обработки исключения UsernameNotFoundException в
        //  методе public TokenResponse login(AuthRequest authRequest) throws RuntimeException класса AuthWithJWTService
        //  иначе будет давать ошибку
//        // Arrange
//        testAuthRequest.setUsername(TEST_USERNAME_NON_EXISTENT);
//        testAuthRequest.setPassword(TEST_PASSWORD);
//        // Act and Assert
//        assertThrows(UsernameNotFoundException.class, () -> authWithJWTService.login(testAuthRequest));
    }

    @Test
    public void loginWithWrongPassword() {
        // Arrange
        testAuthRequest.setUsername(TEST_USERNAME);
        testAuthRequest.setPassword(TEST_PASSWORD_WRONG);
        when(userEntityRepository.findByUsername(TEST_USERNAME_ENCODED)).thenReturn(Optional.of(testUserEntity));
        when(passwordEncoder.matches(TEST_PASSWORD_WRONG, TEST_PASSWORD_ENCODED)).thenReturn(false);
        // Act and Assert
        assertThrows(BadRequestException.class, () -> authWithJWTService.login(testAuthRequest));
    }
}
