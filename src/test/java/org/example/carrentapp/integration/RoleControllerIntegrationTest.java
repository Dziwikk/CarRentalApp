package org.example.carrentapp.integration;

import org.example.carrentapp.entity.Role;
import org.example.carrentapp.repository.RoleRepository;
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
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RoleControllerIntegrationTest {

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
    @Autowired RoleRepository repo;

    private String base;
    private TestRestTemplate adminRest;

    @BeforeEach
    void setUp() {
        repo.deleteAll();
        base = "http://localhost:" + port + "/api/roles";
        adminRest = rest.withBasicAuth("admin", "password");
    }

    @Test
    void listEmpty_andCrudRole() {
        // LIST empty
        ResponseEntity<Role[]> empty = adminRest.getForEntity(base, Role[].class);
        assertThat(empty.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(empty.getBody()).isEmpty();

        // CREATE
        Role toCreate = new Role();
        toCreate.setName("R1");
        ResponseEntity<Role> create = adminRest.postForEntity(base, toCreate, Role.class);
        assertThat(create.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        URI loc = create.getHeaders().getLocation();
        assertThat(loc).isNotNull();
        long id = Long.parseLong(loc.getPath().replaceAll(".*/(\\d+)$","$1"));

        // GET all
        List<Role> all = List.of(adminRest.getForEntity(base, Role[].class).getBody());
        assertThat(all).hasSize(1).first().extracting(Role::getName).isEqualTo("R1");

        // GET by ID
        ResponseEntity<Role> get = adminRest.getForEntity(base + "/" + id, Role.class);
        assertThat(get.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(get.getBody().getName()).isEqualTo("R1");

        // UPDATE
        Role upd = new Role();
        upd.setName("R2");
        HttpEntity<Role> req = new HttpEntity<>(upd, headersJson());
        ResponseEntity<Role> put = adminRest.exchange(base + "/" + id, HttpMethod.PUT, req, Role.class);
        assertThat(put.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(put.getBody().getName()).isEqualTo("R2");

        // DELETE
        ResponseEntity<Void> del = adminRest.exchange(base + "/" + id, HttpMethod.DELETE, null, Void.class);
        assertThat(del.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(adminRest.getForEntity(base + "/" + id, Role.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getRoleById_whenNotExists_shouldReturn404() {
        ResponseEntity<Role> resp = adminRest.getForEntity(base + "/9999", Role.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateRole_whenNotExists_shouldReturn404() {
        Role payload = new Role();
        payload.setName("DoesNotMatter");
        HttpEntity<Role> req = new HttpEntity<>(payload, headersJson());

        ResponseEntity<Role> resp = adminRest.exchange(base + "/8888", HttpMethod.PUT, req, Role.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private HttpHeaders headersJson() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }
}
