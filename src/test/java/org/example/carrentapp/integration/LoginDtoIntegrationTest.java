package org.example.carrentapp.integration;

import org.example.carrentapp.dto.LoginDto;
import org.example.carrentapp.entity.User;
import org.example.carrentapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.boot.test.web.client.TestRestTemplate;


import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LoginDtoIntegrationTest {

    @Container
    static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("test-db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", pg::getJdbcUrl);
        r.add("spring.datasource.username", pg::getUsername);
        r.add("spring.datasource.password", pg::getPassword);
    }

    @LocalServerPort
    int port;
    @Autowired TestRestTemplate rest;  // This import is necessary to fix the "cannot find symbol" error
    @Autowired UserRepository userRepo;
    @Autowired BCryptPasswordEncoder passwordEncoder;

    private String base;
    private String uniqueSuffix;

    @BeforeEach
    void setUp() {
        userRepo.deleteAll();
        uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        base = "http://localhost:" + port + "/api/login";
    }

    @Test
    void login_whenInvalidCredentials_shouldReturnUnauthorized() {
        // Prepare invalid LoginDto
        LoginDto loginDto = new LoginDto();
        loginDto.setUsername("invalid_user");
        loginDto.setPassword("invalid_password");

        // Send login request
        ResponseEntity<String> response = rest.postForEntity(base, loginDto, String.class);

        // Assert response status is unauthorized
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }
}
