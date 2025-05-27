package org.example.carrentapp.integration;

import org.example.carrentapp.entity.Role;
import org.example.carrentapp.entity.User;
import org.example.carrentapp.repository.RoleRepository;
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

import java.util.List;
import java.util.Set;

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
    @Autowired RoleRepository roleRepo;

    private String base;
    private TestRestTemplate adminRest;

    @BeforeEach
    void setUp() {
        userRepo.deleteAll();
        roleRepo.deleteAll();
        base = "http://localhost:" + port + "/api/users";
        adminRest = rest.withBasicAuth("admin", "password");
    }

    @Test
    void crudUser() {
        // seed one role
        Role r = new Role();
        r.setName("ROLE_X");
        r = roleRepo.save(r);

        // LIST empty
        assertThat(adminRest.getForEntity(base, User[].class).getBody()).isEmpty();

        // CREATE user with role
        User toCreate = new User();
        toCreate.setUsername("joe");
        toCreate.setPassword("pw");  // backend powinien zakodować hasło!
        toCreate.setEmail("joe@example.com");
        toCreate.setRoles(Set.of(r));

        ResponseEntity<User> create = adminRest.postForEntity(base, toCreate, User.class);
        assertThat(create.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        Long id = Long.parseLong(
                create.getHeaders().getLocation().getPath().replaceAll(".*/(\\d+)$", "$1")
        );

        // GET all
        List<User> all = List.of(adminRest.getForEntity(base, User[].class).getBody());
        assertThat(all)
                .hasSize(1)
                .first().extracting(User::getUsername).isEqualTo("joe");

        // GET by ID
        ResponseEntity<User> get = adminRest.getForEntity(base + "/" + id, User.class);
        assertThat(get.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(get.getBody().getEmail()).isEqualTo("joe@example.com");

        // UPDATE (change email/username)
        User upd = new User();
        upd.setUsername("jane");
        upd.setPassword("tajne");
        upd.setEmail("jane@example.com");
        // Do NOT set password nor roles here (unless endpoint requires it!)
        HttpEntity<User> req = new HttpEntity<>(upd, headersJson());
        ResponseEntity<User> put = adminRest.exchange(base + "/" + id, HttpMethod.PUT, req, User.class);
        assertThat(put.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(put.getBody())
                .extracting(User::getUsername, User::getEmail)
                .containsExactly("jane","jane@example.com");

        // DELETE
        ResponseEntity<Void> del = adminRest.exchange(base + "/" + id, HttpMethod.DELETE, null, Void.class);
        assertThat(del.getStatusCode()).isEqualTo(HttpStatus.OK);

        // po usunięciu GET by ID zwraca 404
        assertThat(adminRest.getForEntity(base + "/" + id, User.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getUserById_whenNotExists_shouldReturn404() {
        ResponseEntity<User> resp = adminRest.getForEntity(base + "/9999", User.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateUser_whenNotExists_shouldReturn404() {
        User payload = new User();
        payload.setUsername("ghost");
        payload.setEmail("ghost@example.com");
        HttpEntity<User> req = new HttpEntity<>(payload, headersJson());

        ResponseEntity<User> resp = adminRest.exchange(base + "/8888", HttpMethod.PUT, req, User.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private HttpHeaders headersJson() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }
}
