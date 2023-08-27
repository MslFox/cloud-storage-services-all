package com.mslfox.cloudStorageServices.functionalTests;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mslfox.cloudStorageServices.constant.HeaderNameHolder;
import com.mslfox.cloudStorageServices.containers.TestPostgreSQLContainer;
import com.mslfox.cloudStorageServices.dto.auth.AuthRequest;
import com.mslfox.cloudStorageServices.dto.file.FileRequest;
import com.mslfox.cloudStorageServices.entities.auth.UserEntity;
import com.mslfox.cloudStorageServices.entities.jwt.BlackJwtEntity;
import com.mslfox.cloudStorageServices.messages.ErrorMessage;
import com.mslfox.cloudStorageServices.messages.SuccessMessage;
import com.mslfox.cloudStorageServices.model.auth.TokenResponse;
import com.mslfox.cloudStorageServices.model.error.ErrorResponse;
import com.mslfox.cloudStorageServices.model.file.FileInfoResponse;
import com.mslfox.cloudStorageServices.repository.auth.UserEntityRepository;
import com.mslfox.cloudStorageServices.repository.file.FileSystem.FileSystemStorage;
import com.mslfox.cloudStorageServices.repository.jwt.BlackJwtRepository;
import com.mslfox.cloudStorageServices.security.JwtProvider;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static com.mslfox.cloudStorageServices.constant.TestConstantHolder.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "file.system.storage.location=" + TEST_STORAGE_LOCATION,
        "security.jwt.secret=" + TEST_JWT_SECRET})
@TestMethodOrder(MethodOrderer.MethodName.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ModuleTests {
    @Value("${security.jwt.secret}")
    private String secret;
    @Value("${security.jwt.authorities-claim-name}")
    private String authoritiesClaimName;

    @Container
    private static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER_FILE = TestPostgreSQLContainer.getInstance();

    @Autowired
    private UserEntityRepository userEntityRepository;

    @Autowired
    private FileSystemStorage fileSystemStorage;
    @Autowired
    private JwtProvider jwtProvider;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ErrorMessage errorMessage;
    @Autowired
    private SuccessMessage successMessage;
    @Autowired
    private BlackJwtRepository blackJwtRepository;
    private final UserEntity userEntity = new UserEntity(TEST_USERNAME_ENCODED, TEST_PASSWORD_ENCODED);
    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final static MockMultipartFile MULTIPART_FILE = new MockMultipartFile("file", TEST_FILE_CONTENT.getBytes());
    private final static MockMultipartFile MULTIPART_FILE_NULL_CONTENT = new MockMultipartFile("file", (byte[]) null);
    private final static UserEntity USER_ENTITY = new UserEntity(TEST_USERNAME_ENCODED, TEST_PASSWORD_ENCODED);
    private static final String VALID_JWT_WITHOUT_BEARER = generateTestJwt(USER_ENTITY, 60_000);
    private static final String VALID_BEARER_JWT = "Bearer " + VALID_JWT_WITHOUT_BEARER;
    private static final String INVALID_BEARER_JWT = VALID_BEARER_JWT + 1;
    private final static String EXPIRED_BEARER_JWT = "Bearer " + generateTestJwt(USER_ENTITY, 0);
    private final static SimpleDateFormat formatter = new SimpleDateFormat("yyy-MM-dd");

    @BeforeEach
    public void setup() {
        userEntityRepository.save(userEntity);
    }

    @AfterEach
    public void tearDown() {
        userEntityRepository.deleteAll();
    }

    @AfterAll
    public static void deleteStorageFolder() throws IOException {
        FileUtils.forceDelete(new File(TEST_STORAGE_LOCATION));
    }

    @Test
    public void loginSuccess() throws Exception {
        final AuthRequest authRequest = new AuthRequest(TEST_USERNAME, TEST_PASSWORD);
        final var resultActions = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)));
        resultActions.andExpect(status().isOk());
        assertDoesNotThrow(() -> objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsString(), TokenResponse.class));
        final var token = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsString(), TokenResponse.class);
        assertNotNull(token);
    }

    @Test
    public void loginWithUserNotFound() throws Exception {
        // TODO эта проверка может быть включена, только при отключении обработки исключения UsernameNotFoundException в
        //  методе public TokenResponse login(AuthRequest authRequest) throws RuntimeException класса AuthWithJWTService
        //  иначе будет давать ошибку
        //  сейчас иключение обрабаыватся так, что возникновении исключения(не найден пользователь), создается новый пользователь

//        // Arrange
//        final var authRequest = new AuthRequest(TEST_USERNAME_NON_EXISTENT, TEST_PASSWORD);
//        // Act
//        final var resultActions = mockMvc.perform(post("/login")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(authRequest)));
//        // Assert
//        resultActions.andExpect(status().isBadRequest());
//        assertDoesNotThrow(() -> objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsString(), ErrorResponse.class));
//        final var errorResponse = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsString(), ErrorResponse.class);
//        assertNotNull(errorResponse);
//        assertEquals(errorResponse.getMessage(), errorMessage.userNotFound());
    }

    @Test
    public void loginWithWrongPassword() throws Exception {
        // Arrange
        final AuthRequest authRequest = new AuthRequest(TEST_USERNAME, TEST_PASSWORD_WRONG);
        // Act
        final var resultActions = mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)));
        // Assert
        resultActions.andExpect(status().isBadRequest());
        assertDoesNotThrow(() -> objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsString(), ErrorResponse.class));
        final var errorResponse = objectMapper.readValue(resultActions.andReturn().getResponse().getContentAsString(), ErrorResponse.class);
        assertNotNull(errorResponse);
        assertEquals(errorMessage.wrongPassword(), errorResponse.getMessage());
    }

    @Test
    @DisplayName("should save jwt in BlackJwtRepository")
    public void logoutWithValidJWT() throws Exception {
        blackJwtRepository.deleteAll();
        final var validJwtWithoutBearer = jwtProvider.generateJwt(userEntity);
        // Act
        mockMvc.perform(post("/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HeaderNameHolder.TOKEN_HEADER_NAME, "Bearer " + validJwtWithoutBearer));
        assertEquals(validJwtWithoutBearer, blackJwtRepository.findAll().get(0).getToken());
    }

    @Test
    @DisplayName("shouldn't save jwt in BlackJwtRepository")
    public void logoutWithInvalidJWT() throws Exception {
        blackJwtRepository.deleteAll();
        final var validJwtWithoutBearer = jwtProvider.generateJwt(userEntity);
        final var invalidJwtWithBearer = "Bearer " + validJwtWithoutBearer + 1;
        final var validExpiredToken = "Bearer " + Jwts.builder()
                .setSubject(userEntity.getUsername())
                .setExpiration(new Date())
                .signWith(SignatureAlgorithm.HS256, secret)
                .claim(authoritiesClaimName, userEntity.getAuthorities())
                .compact();
        // Act auth-token not start with 'Bearer '
        mockMvc.perform(post("/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HeaderNameHolder.TOKEN_HEADER_NAME, validJwtWithoutBearer));
        // Act auth-token start with 'Bearer ', but not right jwt's format
        mockMvc.perform(post("/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HeaderNameHolder.TOKEN_HEADER_NAME, invalidJwtWithBearer));
        // Act auth-token is right format , but expired
        mockMvc.perform(post("/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .header(HeaderNameHolder.TOKEN_HEADER_NAME, validExpiredToken));
        assert (blackJwtRepository.findAll().size() == 0);
    }


    @Test
    @DisplayName("should return 200 status with List<FileInfoResponse> " + "for 'GET /list?limit={limit}' endpoint with valid 'auth-token'")
    public void limitListUploadedFilesSuccess() throws Exception {
        // Arrange
        final var limit = 1;
        fileSystemStorage.store(TEST_USERNAME_ENCODED, TEST_FILE_NAME, TEST_FILE_CONTENT.getBytes());
        // Act
        final var result = mockMvc.perform(get("/list?limit={limit}", limit)
                .header(HeaderNameHolder.TOKEN_HEADER_NAME, VALID_BEARER_JWT));
        // Assert
        result.andExpect(status().isOk());
        final var expected = objectMapper
                .writeValueAsString(List.of(new FileInfoResponse(TEST_FILE_NAME,
                        (long) TEST_FILE_CONTENT.getBytes().length, formatter.format(new Date()))));
        final var actual = result.andReturn().getResponse().getContentAsString();
        assertEquals(expected, actual);
        deleteUserFolder(TEST_USERNAME_ENCODED);
    }

    @Test
    @DisplayName("should return 400 status with ErrorResponse " + "for 'GET /list?limit={limit}' endpoint with valid 'auth-token'")
    public void limitListUploadedFilesBadRequest() throws Exception {
        // Arrange
        final var urlTemplate = "/list?limit={limit}";
        // Act and Assert
        // parameter 'limit' has invalid zero value
        final var resultWithZeroLimit = mockMvc.perform(get(urlTemplate, 0)
                .header(HeaderNameHolder.TOKEN_HEADER_NAME, VALID_BEARER_JWT));
        assertStatusAndErrorMessage(resultWithZeroLimit, 400, errorMessage.validation());
    }

    @Test
    @DisplayName("should return 200 status with 'success.upload' message " + "for 'POST /file?filename={filename}' endpoint with valid 'auth-token'")
    public void handleFileUploadSuccess() throws Exception {
        // Act
        final var result = mockMvc.perform(multipart("/file")
                .file(MULTIPART_FILE)
                .param("filename", TEST_FILE_NAME)
                .header(HeaderNameHolder.TOKEN_HEADER_NAME, VALID_BEARER_JWT));
        // Assert
        result.andExpect(status().isOk());
        assertEquals(successMessage.getUploadMessage(), result.andReturn().getResponse().getContentAsString());
        deleteUserFolder(TEST_USERNAME_ENCODED);
    }

    @ParameterizedTest
    @DisplayName("should return 400 status with ErrorResponse " + "for 'POST /file?filename={filename}' endpoint with valid 'auth-token'")
    @CsvSource(value = {TEST_FILE_NAME, "' '", ","})
    public void handleFileUploadBadRequest(String filename) throws Exception {
        // Acts
        final var result = mockMvc.perform(multipart("/file")
                .file(MULTIPART_FILE_NULL_CONTENT)
                .param("filename", filename)
                .header(HeaderNameHolder.TOKEN_HEADER_NAME, VALID_BEARER_JWT));
        // Assert
        assertStatusAndErrorMessage(result, 400, errorMessage.validation());
    }

    @Test
    @DisplayName("should return 500 status with ErrorResponse " + "for 'POST /file?filename={filename}' endpoint with valid 'auth-token'")
    public void handleFileUploadServerError() throws Exception {
        // Arrange
        var multipartFile = Mockito.mock(MockMultipartFile.class);
        when(multipartFile.isEmpty()).thenReturn(false);
        when(multipartFile.getName()).thenReturn("file");
        when(multipartFile.getBytes()).thenReturn(null);
        // Acts
        final var result = mockMvc.perform(multipart("/file")
                .file(multipartFile)
                .param("filename", TEST_FILE_NAME)
                .header(HeaderNameHolder.TOKEN_HEADER_NAME, VALID_BEARER_JWT));
        // Assert
        assertStatusAndErrorMessage(result, 500, errorMessage.uploadFile());
    }

    @Test
    @DisplayName("should return 200 status with 'success.delete' message" + "for 'DELETE /file?filename={filename}' endpoint with valid 'auth-token'")
    public void deleteSuccess() throws Exception {
        // Arrange
        fileSystemStorage.store(TEST_USERNAME_ENCODED, TEST_FILE_NAME, TEST_FILE_CONTENT.getBytes());
        final var pathToFile = Path.of(TEST_STORAGE_LOCATION).resolve(TEST_USERNAME_ENCODED);
        // Act
        final var result = mockMvc.perform(delete("/file")
                .param("filename", TEST_FILE_NAME)
                .header(HeaderNameHolder.TOKEN_HEADER_NAME, VALID_BEARER_JWT));
        // Assert
        result.andExpect(status().isOk());
        assertEquals(successMessage.getDeleteMessage(), result.andReturn().getResponse().getContentAsString());
        assert (Objects.requireNonNull(new File(pathToFile.toString()).listFiles()).length == 0);
        deleteUserFolder(TEST_USERNAME_ENCODED);
    }

    @ParameterizedTest
    @CsvSource({"400,' '", "400,''", "400,",})
    @DisplayName("should return 400 status with ErrorResponse " + "for 'DELETE /file?filename={filename}' endpoint with valid 'auth-token'")
    public void deleteBadRequest(int statusCode, String filename) throws Exception {
        // Arrange
        fileSystemStorage.store(TEST_USERNAME_ENCODED, TEST_FILE_NAME, TEST_FILE_CONTENT.getBytes());
        // Act
        final var result = mockMvc.perform(delete("/file")
                .param("filename", filename)
                .header(HeaderNameHolder.TOKEN_HEADER_NAME, VALID_BEARER_JWT));
        // Assert
        assertStatusAndErrorMessage(result, statusCode, errorMessage.validation());
        deleteUserFolder(TEST_USERNAME_ENCODED);
    }

    @Test
    @DisplayName("should return 500 status with ErrorResponse 'error.delete' message " + "for 'DELETE /file?filename={filename}' endpoint with valid 'auth-token'")
    public void deleteServerError() throws Exception {
        // Arrange
        fileSystemStorage.store(TEST_USERNAME_ENCODED, TEST_FILE_NAME, TEST_FILE_CONTENT.getBytes());
        // Act
        final var result = mockMvc.perform(delete("/file")
                .param("filename", TEST_FILE_NAME_NON_EXISTENT)
                .header(HeaderNameHolder.TOKEN_HEADER_NAME, VALID_BEARER_JWT));
        // Assert
        assertStatusAndErrorMessage(result, 500, errorMessage.deleteFile());
        deleteUserFolder(TEST_USERNAME_ENCODED);
    }

    @Test
    @DisplayName("should return 200 status with byte[] file content" + "for 'GET /file?filename={filename}' endpoint with valid 'auth-token'")
    public void serveFileSuccess() throws Exception {
        // Arrange
        fileSystemStorage.store(TEST_USERNAME_ENCODED, TEST_FILE_NAME, TEST_FILE_CONTENT.getBytes());
        // Act
        final var result = mockMvc.perform(get("/file")
                .param("filename", TEST_FILE_NAME)
                .header(HeaderNameHolder.TOKEN_HEADER_NAME, VALID_BEARER_JWT));
        // Assert
        result.andExpect(status().isOk());
        deleteUserFolder(TEST_USERNAME_ENCODED);
    }

    @ParameterizedTest
    @CsvSource({"' '", "''", ",",})
    @DisplayName("should return 400 status with ErrorResponse for 'GET /file?filename={filename}' endpoint with valid 'auth-token'")
    public void serveFileBadRequest(String filename) throws Exception {
        // Act
        final var result = mockMvc.perform(get("/file")
                .param("filename", filename)
                .header(HeaderNameHolder.TOKEN_HEADER_NAME, VALID_BEARER_JWT));
        // Assert
        assertStatusAndErrorMessage(result, 400, errorMessage.validation());
    }

    @Test
    @DisplayName("should return 500 status with ErrorResponse " + "for 'GET /file?filename={filename}' endpoint with valid 'auth-token'")
    public void serveFileServerError() throws Exception {
        // Act
        final var result = mockMvc.perform(get("/file")
                .param("filename", TEST_FILE_NAME)
                .header(HeaderNameHolder.TOKEN_HEADER_NAME, VALID_BEARER_JWT));
        // Assert
        assertStatusAndErrorMessage(result, 500, errorMessage.gettingFile());
    }

    @Test
    @DisplayName("should return 200 status with byte[] file content" + "for 'PUT /file?filename={filename}' endpoint with valid 'auth-token'")
    public void handleFileRenameSuccess() throws Exception {
        // Arrange
        fileSystemStorage.store(TEST_USERNAME_ENCODED, TEST_FILE_NAME, TEST_FILE_CONTENT.getBytes());
        final var renamedFilePath =
                Path.of(TEST_STORAGE_LOCATION).resolve(TEST_USERNAME_ENCODED).resolve(TEST_FILE_RENAMED_NAME);
        // Act
        final var result = mockMvc.perform(put("/file")
                .param("filename", TEST_FILE_NAME)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new FileRequest(TEST_FILE_RENAMED_NAME)))
                .header(HeaderNameHolder.TOKEN_HEADER_NAME, VALID_BEARER_JWT));
        // Assert
        result.andExpect(status().isOk());
        assertEquals(successMessage.getRenameMessage(), result.andReturn().getResponse().getContentAsString());
        assert (renamedFilePath.toFile().exists());
        assertArrayEquals(Files.readAllBytes(renamedFilePath), TEST_FILE_CONTENT.getBytes());
        deleteUserFolder(TEST_USERNAME_ENCODED);
    }

    @ParameterizedTest
    @CsvSource({
            "," + TEST_FILE_RENAMED_NAME,
            "''," + TEST_FILE_RENAMED_NAME,
            "' '," + TEST_FILE_RENAMED_NAME,
            TEST_FILE_NAME + ",",
            TEST_FILE_NAME + ",''",
            TEST_FILE_NAME + ",' '"})
    @DisplayName("should return 400 status with ErrorResponse " + "for 'PUT /file?filename={filename}' endpoint with valid 'auth-token'")
    public void handleFileRenameBadRequest(String toUpdateFilename, String newFileName) throws Exception {
        // Act
        final var result = mockMvc.perform(put("/file")
                .param("filename", toUpdateFilename)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new FileRequest(newFileName)))
                .header(HeaderNameHolder.TOKEN_HEADER_NAME, VALID_BEARER_JWT));
        // Assert
        assertStatusAndErrorMessage(result, 400, errorMessage.validation());
    }

    @Test
    @DisplayName("should return 500 status with ErrorResponse " + "for 'PUT /file?filename={filename}' endpoint with valid 'auth-token'")
    public void handleFileRenameServerError() throws Exception {
        // Act
        final var result = mockMvc.perform(put("/file")
                .param("filename", TEST_FILE_NAME_NON_EXISTENT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new FileRequest(TEST_FILE_RENAMED_NAME)))
                .header(HeaderNameHolder.TOKEN_HEADER_NAME, VALID_BEARER_JWT));
        // Assert
        assertStatusAndErrorMessage(result, 500, errorMessage.renameFile());
    }


    @ParameterizedTest
    @CsvSource({"GET, /list", "GET,/file", "POST,/file", "PUT,/file", "DELETE,/file",})
    @DisplayName("should return 401 status with ErrorResponse for all FileController endpoints")
    public void shouldReturnUnauthorizedStatusWithErrorResponse(@NonNull HttpMethod httpMethod, String urlTemplate) throws Exception {
        // Arrange
        String[][] tokenAndErrorMessagePairs = new String[][]{{VALID_JWT_WITHOUT_BEARER, errorMessage.jwtStartBearer()},
                {INVALID_BEARER_JWT, "JWT validity cannot be asserted"},
                {EXPIRED_BEARER_JWT, "JWT expired"},
                {VALID_BEARER_JWT, errorMessage.getJwtBlacklisted()}};
        final var currentBlackJwt = BlackJwtEntity.builder()
                .token(VALID_JWT_WITHOUT_BEARER)
                .expiration(jwtProvider.getClaims(VALID_JWT_WITHOUT_BEARER).getExpiration().getTime()).build();

        blackJwtRepository.save(currentBlackJwt);
        // Act and Assert
        switch (httpMethod) {
            case GET -> {
                for (String[] tokenAndErrorMessagePair : tokenAndErrorMessagePairs) {
                    final var result = mockMvc.perform(get(urlTemplate)
                            .header(HeaderNameHolder.TOKEN_HEADER_NAME, tokenAndErrorMessagePair[0]));
                    assertStatusAndErrorMessage(result, 401, tokenAndErrorMessagePair[1]);
                }
            }
            case POST -> {
                for (String[] tokenAndErrorMessagePair : tokenAndErrorMessagePairs) {
                    final var result = mockMvc.perform(post(urlTemplate)
                            .header(HeaderNameHolder.TOKEN_HEADER_NAME, tokenAndErrorMessagePair[0]));
                    assertStatusAndErrorMessage(result, 401, tokenAndErrorMessagePair[1]);
                }
            }
            case PUT -> {
                for (String[] tokenAndErrorMessagePair : tokenAndErrorMessagePairs) {
                    final var result = mockMvc.perform(put(urlTemplate)
                            .header(HeaderNameHolder.TOKEN_HEADER_NAME, tokenAndErrorMessagePair[0]));
                    assertStatusAndErrorMessage(result, 401, tokenAndErrorMessagePair[1]);
                }
            }
            case DELETE -> {
                for (String[] tokenAndErrorMessagePair : tokenAndErrorMessagePairs) {
                    final var result = mockMvc.perform(delete(urlTemplate)
                            .header(HeaderNameHolder.TOKEN_HEADER_NAME, tokenAndErrorMessagePair[0]));
                    assertStatusAndErrorMessage(result, 401, tokenAndErrorMessagePair[1]);
                }
            }
        }
        blackJwtRepository.deleteAll();
    }

    private void assertStatusAndErrorMessage(@NonNull ResultActions resultActions, int expectedStatus, String expectedMessage) throws Exception {
        resultActions.andExpect(status().is(expectedStatus));
        final var result = resultActions.andReturn().getResponse().getContentAsString();
        final var errorResponse = objectMapper.readValue(result, ErrorResponse.class);
        assertNotNull(errorResponse);
        assertTrue(errorResponse.getMessage().contains(expectedMessage));
    }

    private static void deleteUserFolder(String encodedUsername) throws IOException {
        FileUtils.forceDelete(new File(TEST_STORAGE_LOCATION + "/" + encodedUsername));
    }

    private static String generateTestJwt(@NonNull UserDetails userDetails, long tokenLifeTimeMilliSeconds) {
        String username = userDetails.getUsername();
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + tokenLifeTimeMilliSeconds);
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, TEST_JWT_SECRET)
                .claim("roles", userDetails.getAuthorities())
                .compact();
    }
}