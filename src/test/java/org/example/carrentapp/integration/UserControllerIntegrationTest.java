// src/test/java/org/example/carrentapp/integration/UserControllerIntegrationTest.java
package org.example.carrentapp.integration;

import org.example.carrentapp.dto.UserDto;
import org.example.carrentapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.URI;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("test-db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url",     pg::getJdbcUrl);
        r.add("spring.datasource.username",pg::getUsername);
        r.add("spring.datasource.password",pg::getPassword);
    }

    @LocalServerPort int port;
    @Autowired TestRestTemplate rest;
    @Autowired UserRepository userRepo;

    private String base;
    private TestRestTemplate adminRest;
    private String uniqueSuffix;

    @BeforeEach
    void setUp() {
        userRepo.deleteAll();
        uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        base = "http://localhost:" + port + "/api/users";
        adminRest = rest.withBasicAuth("admin", "password");
    }

    @Test
    void listUsers_whenEmpty_shouldReturnEmptyArray() {
        ResponseEntity<UserDto[]> response = adminRest.getForEntity(base, UserDto[].class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void crudUserDto_shouldCoverAllFields() {
        // CREATE
        UserDto toCreate = new UserDto();
        toCreate.setUsername("user_" + uniqueSuffix);
        toCreate.setPassword("pwd");
        toCreate.setEmail("user_" + uniqueSuffix + "@example.com");

        ResponseEntity<UserDto> create = adminRest.postForEntity(base, toCreate, UserDto.class);
        assertThat(create.getStatusCode()).isEqualTo(HttpStatus.OK); // zamiast 201 zwraca 200;
        UserDto created = create.getBody();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getUsername()).isEqualTo(toCreate.getUsername());
        assertThat(created.getEmail()).isEqualTo(toCreate.getEmail());

        Long id = created.getId();

        // GET by ID
        ResponseEntity<UserDto> get = adminRest.getForEntity(base + "/" + id, UserDto.class);
        assertThat(get.getStatusCode()).isEqualTo(HttpStatus.OK);
        UserDto fetched = get.getBody();
        assertThat(fetched.getId()).isEqualTo(created.getId());
        assertThat(fetched.getUsername()).isEqualTo(created.getUsername());
        assertThat(fetched.getEmail()).isEqualTo(created.getEmail());

        // UPDATE
        UserDto upd = new UserDto();
        String newSuffix = UUID.randomUUID().toString().substring(0, 8);
        upd.setUsername("user_" + newSuffix);
        upd.setEmail("user_" + newSuffix + "@example.com");
        HttpHeaders headers = new HttpHeaders(); headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<UserDto> req = new HttpEntity<>(upd, headers);

        ResponseEntity<UserDto> put = adminRest.exchange(base + "/" + id, HttpMethod.PUT, req, UserDto.class);
        assertThat(put.getStatusCode()).isEqualTo(HttpStatus.OK);
        UserDto updated = put.getBody();
        assert updated != null;
        assertThat(updated.getUsername()).isEqualTo(upd.getUsername());
        assertThat(updated.getEmail()).isEqualTo(upd.getEmail());

        // DELETE
        ResponseEntity<Void> del = adminRest.exchange(base + "/" + id, HttpMethod.DELETE, null, Void.class);
        assertThat(del.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(adminRest.getForEntity(base + "/" + id, UserDto.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }
}
