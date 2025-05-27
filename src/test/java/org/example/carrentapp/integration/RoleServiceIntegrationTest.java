// src/test/java/org/example/carrentapp/integration/RoleServiceIntegrationTest.java
package org.example.carrentapp.integration;

import org.example.carrentapp.entity.Role;
import org.example.carrentapp.repository.RoleRepository;
import org.example.carrentapp.service.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Testcontainers
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RoleServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("test-db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url",     postgres::getJdbcUrl);
        r.add("spring.datasource.username",postgres::getUsername);
        r.add("spring.datasource.password",postgres::getPassword);
    }

    @Autowired private RoleService roleService;
    @Autowired private RoleRepository roleRepo;

    @BeforeEach
    void setUp() {
        roleRepo.deleteAll();
    }

    @Test
    void createAndGetAllRoles() {
        Role r = new Role();
        r.setName("R1");
        Role saved = roleService.createRole(r);
        assertThat(saved.getId()).isNotNull();

        List<Role> all = roleService.getAllRoles();
        assertThat(all).hasSize(1).first().extracting(Role::getName).isEqualTo("R1");
    }

    @Test
    void get_update_delete() {
        Role base = new Role(); base.setName("X");
        Role created = roleService.createRole(base);

        Role fetched = roleService.getRoleById(created.getId());
        assertThat(fetched).isNotNull().extracting(Role::getName).isEqualTo("X");

        Role payload = new Role(); payload.setName("Y");
        Role updated = roleService.updateRole(created.getId(), payload);
        assertThat(updated.getName()).isEqualTo("Y");

        // usuń istniejącą
        boolean deleted = roleService.deleteRole(created.getId());
        assertThat(deleted).isTrue();
        assertThat(roleService.getRoleById(created.getId())).isNull();
    }

    @Test
    void deleteRole_whenNotExists_returnsFalse() {
        // teraz testujemy ścieżkę "return false"
        boolean deleted = roleService.deleteRole(9999L);
        assertThat(deleted).isFalse();
    }
}
