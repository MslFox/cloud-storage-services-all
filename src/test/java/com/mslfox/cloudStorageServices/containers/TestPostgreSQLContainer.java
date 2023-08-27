package com.mslfox.cloudStorageServices.containers;

import org.testcontainers.containers.PostgreSQLContainer;

public class TestPostgreSQLContainer {

    private final static PostgreSQLContainer<?> postgreSQLContainer;

    static {
        postgreSQLContainer = new PostgreSQLContainer<>("postgres:15.1")
                .withDatabaseName("testDB")
                .withUsername("testDB")
                .withPassword("testDB");
        postgreSQLContainer.start();
        System.setProperty("spring.datasource.url", postgreSQLContainer.getJdbcUrl());
        System.setProperty("spring.datasource.username", postgreSQLContainer.getUsername());
        System.setProperty("spring.datasource.password", postgreSQLContainer.getPassword());
    }

    public static PostgreSQLContainer<?> getInstance() {
        return postgreSQLContainer;
    }
}