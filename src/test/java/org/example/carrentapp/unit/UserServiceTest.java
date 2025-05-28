package org.example.carrentapp.unit;

import org.example.carrentapp.entity.Role;
import org.example.carrentapp.entity.User;
import org.example.carrentapp.repository.RoleRepository;
import org.example.carrentapp.repository.UserRepository;
import org.example.carrentapp.service.UserService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class UserServiceTest {
    @Mock private UserRepository userRepo;
    @Mock private RoleRepository roleRepo;
    @Mock private PasswordEncoder encoder;

    @InjectMocks private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createUser_successfulWithoutRoles() {
        User input = new User();
        input.setId(42L);
        input.setUsername("alice");
        input.setEmail("alice@example.com");
        input.setPassword("plain");

        when(userRepo.findByUsername("alice")).thenReturn(Optional.empty());
        when(userRepo.existsByEmail("alice@example.com")).thenReturn(false);
        when(encoder.encode("plain")).thenReturn("encoded");
        when(userRepo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.createUser(input);

        assertNull(result.getId());
        assertEquals("encoded", result.getPassword());
        assertEquals("alice", result.getUsername());
        assertEquals("alice@example.com", result.getEmail());
        assertTrue(result.getRoles() == null || result.getRoles().isEmpty());

        verify(userRepo).save(result);
    }

    @Test
    void createUser_withRoles_resolvesAndAssigns() {
        User input = new User();
        input.setUsername("bob");
        input.setEmail("bob@example.com");
        input.setPassword("pw");

        Role r1 = new Role(); r1.setId(1L);
        Role r2 = new Role(); r2.setId(2L);
        input.setRoles(new HashSet<>(Arrays.asList(r1, r2)));

        when(userRepo.findByUsername("bob")).thenReturn(Optional.empty());
        when(userRepo.existsByEmail("bob@example.com")).thenReturn(false);
        when(encoder.encode("pw")).thenReturn("encoded2");
        when(roleRepo.findById(1L)).thenReturn(Optional.of(r1));
        when(roleRepo.findById(2L)).thenReturn(Optional.of(r2));
        when(userRepo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.createUser(input);

        assertEquals("encoded2", result.getPassword());
        assertNotNull(result.getRoles());
        assertEquals(2, result.getRoles().size());
        assertTrue(result.getRoles().contains(r1));
        assertTrue(result.getRoles().contains(r2));
    }

    @Test
    void createUser_nullOrBlankPassword_throws() {
        User u1 = new User(); u1.setPassword(null);
        User u2 = new User(); u2.setPassword("   ");

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(u1));
        assertThrows(IllegalArgumentException.class, () -> userService.createUser(u2));
    }

    @Test
    void createUser_duplicateUsername_throws() {
        User input = new User();
        input.setUsername("charlie"); input.setPassword("x");
        when(userRepo.findByUsername("charlie")).thenReturn(Optional.of(new User()));

        assertThrows(DuplicateKeyException.class, () -> userService.createUser(input));
    }

    @Test
    void createUser_duplicateEmail_throws() {
        User input = new User();
        input.setUsername("dave"); input.setPassword("y"); input.setEmail("dave@example.com");
        when(userRepo.findByUsername("dave")).thenReturn(Optional.empty());
        when(userRepo.existsByEmail("dave@example.com")).thenReturn(true);

        assertThrows(DuplicateKeyException.class, () -> userService.createUser(input));
    }

    @Test
    void createUser_roleNotFound_throws() {
        User input = new User();
        input.setUsername("eve"); input.setEmail("eve@example.com"); input.setPassword("z");
        Role r = new Role(); r.setId(999L);
        input.setRoles(Collections.singleton(r));

        when(userRepo.findByUsername("eve")).thenReturn(Optional.empty());
        when(userRepo.existsByEmail("eve@example.com")).thenReturn(false);
        when(roleRepo.findById(999L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(input));
    }

    // Tests for getUserById
    @Test
    void getUserById_found_returnsUser() {
        User u = new User();
        u.setId(7L);
        when(userRepo.findById(7L)).thenReturn(Optional.of(u));

        User result = userService.getUserById(7L);
        assertNotNull(result);
        assertEquals(7L, result.getId());
    }

    @Test
    void getUserById_notFound_returnsNull() {
        when(userRepo.findById(8L)).thenReturn(Optional.empty());

        User result = userService.getUserById(8L);
        assertNull(result);
    }

    // Tests for updateUser
    @Test
    void updateUser_found_updatesAndReturns() {
        User existing = new User(); existing.setId(9L);
        existing.setUsername("orig"); existing.setEmail("orig@example.com");
        User payload = new User(); payload.setUsername("new"); payload.setEmail("new@example.com");

        when(userRepo.findById(9L)).thenReturn(Optional.of(existing));
        when(userRepo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userService.updateUser(9L, payload);
        assertNotNull(result);
        assertEquals("new", result.getUsername());
        assertEquals("new@example.com", result.getEmail());
    }

    @Test
    void updateUser_notFound_returnsNull() {
        when(userRepo.findById(10L)).thenReturn(Optional.empty());

        User result = userService.updateUser(10L, new User());
        assertNull(result);
    }

    // Tests for deleteUser
    @Test
    void deleteUser_exists_deletesAndReturnsTrue() {
        when(userRepo.existsById(11L)).thenReturn(true);
        doNothing().when(userRepo).deleteById(11L);

        boolean result = userService.deleteUser(11L);
        assertTrue(result);
        verify(userRepo).deleteById(11L);
    }

    @Test
    void deleteUser_notExists_returnsFalse() {
        when(userRepo.existsById(12L)).thenReturn(false);

        boolean result = userService.deleteUser(12L);
        assertFalse(result);
        verify(userRepo, never()).deleteById(anyLong());
    }
}
