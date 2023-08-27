package com.mslfox.cloudStorageServices.constant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TestConstantHolder {
    public final static String TEST_STORAGE_LOCATION = "test_uploadStorage";
    public final static String TEST_JWT = "test_Jwt";
    public final static String TEST_JWT_SECRET =  "TestSecret+TestSecret+TestSecret+TestSecret+TestSecret";
    public final static String TEST_USERNAME = "test@test.com";
    public final static String TEST_USERNAME_ENCODED = "dGVzdEB0ZXN0LmNvbQ==";
    public final static String TEST_USERNAME_NON_EXISTENT = "nonExistantT@test.com";
    public final static String TEST_PASSWORD = "t1Test";
    public final static String TEST_PASSWORD_ENCODED = "$2a$12$Nnu6cbxPHX3gwi2l8c9D1uqGgwkiekhCRMyZNO0FzPxwCkKya//ZO";
    public final static String TEST_PASSWORD_WRONG = "wrong_t1Test";
    public final static String TEST_BEARER_JWT = "Bearer " + TEST_JWT;
    public final static String TEST_URI = "/testUri";
    public final static String TEST_FILE_NAME = "test.txt";
    public final static String TEST_FILE_NAME_NON_EXISTENT = "NonExistentTest.txt";
    public final static String TEST_FILE_CONTENT = "Test file content";
    public final static String TEST_FILE_DATE = "23-23-23";
    public final static String TEST_FILE_RENAMED_NAME = "renamed_test.txt";
    public final static String TEST_EXCEPTION_MESSAGE = "Test exception message";
    public final static String TEST_SUCCESS_MESSAGE = "Test success message";

}
