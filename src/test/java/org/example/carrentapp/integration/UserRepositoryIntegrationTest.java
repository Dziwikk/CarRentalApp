// src/test/java/org/example/carrentapp/integration/UserRepositoryIntegrationTest.java
package org.example.carrentapp.integration;

import org.example.carrentapp.entity.Role;
import org.example.carrentapp.entity.User;
import org.example.carrentapp.repository.RoleRepository;
import org.example.carrentapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("test-db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",    postgres::getJdbcUrl);
        registry.add("spring.datasource.username",postgres::getUsername);
        registry.add("spring.datasource.password",postgres::getPassword);
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    private User user;

    @BeforeEach
    void setUp() {
        // 1. tworzysz rolę bezargumentowo i ustawiasz nazwę
        Role roleUser = new Role();
        roleUser.setName("ROLE_USER");
        roleUser = roleRepository.save(roleUser);

        // 2. budujesz użytkownika i przypisujesz persistentną rolę
        user = new User();
        user.setUsername("jan");
        user.setPassword("secret");
        user.setEmail("jan@example.com");
        user.getRoles().add(roleUser);
    }

    @Test
    void findByUsername_existingUser_shouldReturnUser() {
        // given
        User saved = userRepository.save(user);

        // when
        Optional<User> loaded = userRepository.findByUsername("jan");

        // then
        assertThat(loaded).isPresent();
        User u = loaded.get();
        assertThat(u.getId()).isEqualTo(saved.getId());
        assertThat(u.getEmail()).isEqualTo("jan@example.com");
        assertThat(u.getRoles())
                .extracting(Role::getName)
                .containsExactly("ROLE_USER");
    }

    @Test
    void findByUsername_nonExistingUser_shouldBeEmpty() {
        assertThat(userRepository.findByUsername("nieistnieje")).isEmpty();
    }
}
