package com.mslfox.cloudStorageServices.controller.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mslfox.cloudStorageServices.constant.HeaderNameHolder;
import com.mslfox.cloudStorageServices.dto.auth.AuthRequest;
import com.mslfox.cloudStorageServices.model.auth.TokenResponse;
import com.mslfox.cloudStorageServices.service.auth.impl.AuthWithJWTService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static com.mslfox.cloudStorageServices.constant.TestConstantHolder.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthControllerTest {
    private static AuthWithJWTService mockAuthWithJWTService;
    private static MockMvc mockMvc;
    private static ObjectMapper objectMapper;

    @BeforeAll
    public static void setup() {
        mockAuthWithJWTService = Mockito.mock(AuthWithJWTService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(mockAuthWithJWTService)).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    public void loginSuccess() throws Exception {
        // Arrange
        final var authRequest = new AuthRequest(TEST_USERNAME, TEST_PASSWORD);
        final var tokenResponse = new TokenResponse(TEST_JWT);
        when(mockAuthWithJWTService.login(any(AuthRequest.class))).thenReturn(tokenResponse);
        // Act
        final var resultActions = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)));
        // Assert
        resultActions.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath(HeaderNameHolder.TOKEN_HEADER_NAME).value(TEST_JWT));

        verify(mockAuthWithJWTService).login(authRequest);
    }

    @ParameterizedTest
    @CsvSource(value = {
            ",",
            "' ',' '",
            "test, test",})
    public void loginBadRequest(String username, String password) throws Exception {
        // Arrange
        final var authRequest = new AuthRequest(username, password);
        // Act
        final var resultActions = mockMvc.perform(post("/login")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(authRequest)));
        // Assert
        resultActions
                .andExpect(status().isBadRequest());
    }
}
