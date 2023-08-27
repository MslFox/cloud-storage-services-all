package com.mslfox.cloudStorageServices.security;

import com.mslfox.cloudStorageServices.entities.jwt.BlackJwtEntity;
import com.mslfox.cloudStorageServices.repository.jwt.BlackJwtRepository;
import io.jsonwebtoken.impl.DefaultClaims;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;

import static com.mslfox.cloudStorageServices.constant.TestConstantHolder.TEST_EXCEPTION_MESSAGE;
import static com.mslfox.cloudStorageServices.constant.TestConstantHolder.TEST_JWT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LogoutHandlerWithJWTBlacklistTest {
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private JwtFilter jwtFilter;
    @Mock
    private BlackJwtRepository blackJwtRepository;
    @InjectMocks
    private LogoutHandlerWithJWTBlacklist logoutHandlerWithJWTBlacklist;
    @Mock
    private HttpServletRequest mockHttpServletRequest;
    @Mock
    private  HttpServletResponse mockHttpServletResponse;
    @Mock
    private  Authentication mockAuthentication;

    @Test
    public void testOnLogoutSuccessWithValidJwt() throws Exception {
        // Arrange
        final var claims = new DefaultClaims();
        claims.setExpiration(new Date(System.currentTimeMillis() + 6000L));
        when(jwtFilter.getJwt(mockHttpServletRequest)).thenReturn(TEST_JWT);
        when(jwtProvider.validateJwt(TEST_JWT)).thenReturn(true);
        when(jwtProvider.getClaims(TEST_JWT)).thenReturn(claims);
        SecurityContextHolder.getContext().setAuthentication(mockAuthentication);
        // Act
        logoutHandlerWithJWTBlacklist.onLogoutSuccess(mockHttpServletRequest, mockHttpServletResponse, mockAuthentication);
        // Assert
        verify(blackJwtRepository, times(1)).save(any(BlackJwtEntity.class));
        Thread.sleep(100L);
        verify(blackJwtRepository).deleteByExpirationBefore(anyLong());
        Assertions.assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    public void testOnLogoutSuccessWithInvalidJwt() {
        // Arrange
        when(jwtFilter.getJwt(mockHttpServletRequest)).thenReturn(TEST_JWT);
        when(jwtProvider.validateJwt(TEST_JWT)).thenThrow(new RuntimeException(TEST_EXCEPTION_MESSAGE));
        // Act
        logoutHandlerWithJWTBlacklist.onLogoutSuccess(mockHttpServletRequest, mockHttpServletResponse, mockAuthentication);
        // Assert
        verify(jwtProvider, never()).getClaims(anyString());
        verify(blackJwtRepository, never()).save(any(BlackJwtEntity.class));
        verify(blackJwtRepository, never()).deleteByExpirationBefore(anyLong());
    }
}