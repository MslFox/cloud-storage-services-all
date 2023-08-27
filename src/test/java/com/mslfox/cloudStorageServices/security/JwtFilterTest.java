package com.mslfox.cloudStorageServices.security;

import com.mslfox.cloudStorageServices.constant.HeaderNameHolder;
import com.mslfox.cloudStorageServices.messages.ErrorMessage;
import com.mslfox.cloudStorageServices.model.error.ErrorResponse;
import io.jsonwebtoken.SignatureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

import static com.mslfox.cloudStorageServices.constant.TestConstantHolder.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JwtFilterTest {
    @Mock
    private JwtProvider jwtProvider;
    @Mock
    private ErrorMessage errorMessage;
    @InjectMocks
    private JwtFilter jwtFilter;
    @Mock
    private HttpServletRequest httpServletRequest;
    @Mock
    private HttpServletResponse httpServletResponse;
    @Mock
    private FilterChain filterChain;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        when(httpServletRequest.getHeader(HeaderNameHolder.TOKEN_HEADER_NAME)).thenReturn(TEST_BEARER_JWT);
    }


    @Test
    public void getJwtSuccess() throws SignatureException {
        // Act
        final var result = jwtFilter.getJwt(httpServletRequest);
        // Assert
        assertEquals(TEST_JWT, result);
    }
    @Test
    public void getJwtWithException() {
        when(httpServletRequest.getHeader(HeaderNameHolder.TOKEN_HEADER_NAME)).thenReturn(TEST_JWT);
        when(errorMessage.jwtStartBearer()).thenReturn(TEST_EXCEPTION_MESSAGE);
        // Act and Assert
        final var exception = assertThrows(SignatureException.class, () -> jwtFilter.getJwt(httpServletRequest));
        assertEquals(TEST_EXCEPTION_MESSAGE, exception.getMessage());
    }

    @Test
    public void doFilterInternalSuccess() throws Exception {
        // Arrange
        when(httpServletRequest.getRequestURI()).thenReturn(TEST_URI);
        // Act
        jwtFilter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);
        // Assert
        verify(filterChain).doFilter(httpServletRequest, httpServletResponse);
    }

    @Test
    public void doFilterInternalWithException() throws Exception {
        // Arrange
        when(httpServletRequest.getRequestURI()).thenReturn(TEST_URI);
        when(jwtProvider.validateJwt(anyString())).thenThrow(new RuntimeException(TEST_EXCEPTION_MESSAGE));
        when(httpServletResponse.getWriter()).thenReturn(mock(PrintWriter.class));
        // Act
        jwtFilter.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);
        // Assert
        verify(httpServletResponse).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(httpServletResponse.getWriter()).write(objectMapper.writeValueAsString(new ErrorResponse(TEST_EXCEPTION_MESSAGE, 1L)));
    }
}