package org.example.carrentapp.all;

import org.example.carrentapp.entity.User;
import org.example.carrentapp.repository.UserRepository;
import org.example.carrentapp.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository repo;

    @InjectMocks
    private UserService userService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User();
        sampleUser.setId(42L);
        sampleUser.setUsername("alice");
        sampleUser.setEmail("alice@example.com");
    }

    @Test
    void getAllUsers_shouldReturnListFromRepo() {
        List<User> users = Arrays.asList(sampleUser);
        when(repo.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();

        assertThat(result).isSameAs(users);
        verify(repo).findAll();
    }

    @Test
    void createUser_shouldIgnoreClientIdAndSave() {
        User incoming = new User();
        incoming.setId(99L);
        incoming.setUsername("bob");
        incoming.setEmail("bob@example.com");

        User saved = new User();
        saved.setId(2L);
        saved.setUsername("bob");
        saved.setEmail("bob@example.com");

        when(repo.save(any(User.class))).thenReturn(saved);

        User result = userService.createUser(incoming);

        // incoming id must be nulled before save
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(repo).save(captor.capture());
        assertThat(captor.getValue().getId()).isNull();
        assertThat(result).isEqualTo(saved);
    }

    @Test
    void getUserById_whenExists_shouldReturnUser() {
        when(repo.findById(42L)).thenReturn(Optional.of(sampleUser));

        User result = userService.getUserById(42L);

        assertThat(result).isSameAs(sampleUser);
        verify(repo).findById(42L);
    }

    @Test
    void getUserById_whenNotExists_shouldReturnNull() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        User result = userService.getUserById(99L);

        assertThat(result).isNull();
        verify(repo).findById(99L);
    }

    @Test
    void updateUser_whenExists_shouldModifyAndSave() {
        User payload = new User();
        payload.setUsername("charlie");
        payload.setEmail("charlie@example.com");

        when(repo.findById(42L)).thenReturn(Optional.of(sampleUser));
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUser(42L, payload);

        assertThat(result.getUsername()).isEqualTo("charlie");
        assertThat(result.getEmail()).isEqualTo("charlie@example.com");
        verify(repo).save(sampleUser);
    }

    @Test
    void updateUser_whenNotExists_shouldReturnNull() {
        when(repo.findById(100L)).thenReturn(Optional.empty());

        User result = userService.updateUser(100L, new User());

        assertThat(result).isNull();
        verify(repo, never()).save(any());
    }

    @Test
    void deleteUser_whenExists_shouldDeleteAndReturnTrue() {
        when(repo.existsById(42L)).thenReturn(true);

        boolean result = userService.deleteUser(42L);

        assertThat(result).isTrue();
        verify(repo).deleteById(42L);
    }

    @Test
    void deleteUser_whenNotExists_shouldReturnFalse() {
        when(repo.existsById(99L)).thenReturn(false);

        boolean result = userService.deleteUser(99L);

        assertThat(result).isFalse();
        verify(repo, never()).deleteById(anyLong());
    }
}