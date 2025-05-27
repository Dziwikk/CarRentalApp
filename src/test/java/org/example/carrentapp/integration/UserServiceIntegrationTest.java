// src/test/java/org/example/carrentapp/integration/UserServiceIntegrationTest.java
package org.example.carrentapp.integration;

import org.example.carrentapp.entity.Role;
import org.example.carrentapp.entity.User;
import org.example.carrentapp.repository.RoleRepository;
import org.example.carrentapp.repository.UserRepository;
import org.example.carrentapp.service.UserService;
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

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@Testcontainers
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserServiceIntegrationTest {

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

    @Autowired private UserService userService;
    @Autowired private UserRepository userRepo;
    @Autowired private RoleRepository roleRepo;

    @BeforeEach
    void setUp() {
        userRepo.deleteAll();
        roleRepo.deleteAll();
    }

    @Test
    void createAndGetAllUsers() {
        Role role = new Role();
        role.setName("ROLE_U");
        role = roleRepo.save(role);

        User u = new User();
        u.setUsername("joe");
        u.setPassword("pw");
        u.setEmail("joe@example.com");
        u.getRoles().add(role);

        User created = userService.createUser(u);
        assertThat(created.getId()).isNotNull();

        var all = userService.getAllUsers();
        assertThat(all).hasSize(1)
                .first()
                .extracting(User::getUsername)
                .isEqualTo("joe");
    }

    @Test
    void get_update_deleteExistingUser() {
        Role role = new Role();
        role.setName("ROLE_A");
        role = roleRepo.save(role);

        User u = new User();
        u.setUsername("ann");
        u.setPassword("pw");
        u.setEmail("ann@example.com");
        u.getRoles().add(role);

        User created = userService.createUser(u);

        var fetched = userService.getUserById(created.getId());
        assertThat(fetched.getUsername()).isEqualTo("ann");

        User payload = new User();
        payload.setUsername("anna");
        payload.setEmail("anna@example.com");
        var updated = userService.updateUser(created.getId(), payload);
        assertThat(updated.getUsername()).isEqualTo("anna");
        assertThat(updated.getEmail()).isEqualTo("anna@example.com");

        boolean deleted = userService.deleteUser(created.getId());
        assertThat(deleted).isTrue();
        assertThat(userService.getUserById(created.getId())).isNull();
    }

    @Test
    void deleteUser_whenNotExists_returnsFalse() {
        boolean deleted = userService.deleteUser(12345L);
        assertThat(deleted).isFalse();
    }
}
