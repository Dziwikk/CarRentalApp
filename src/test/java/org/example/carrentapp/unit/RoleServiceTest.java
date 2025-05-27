package org.example.carrentapp.unit;

import org.example.carrentapp.entity.Role;
import org.example.carrentapp.repository.RoleRepository;
import org.example.carrentapp.service.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@Nested
@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock
    private RoleRepository repo;

    @InjectMocks
    private RoleService roleService;

    private Role sampleRole;

    @BeforeEach
    void setUp() {
        sampleRole = new Role();
        sampleRole.setId(1L);
        sampleRole.setName("ADMIN");
    }

    @Test
    void getAllRoles_shouldReturnListFromRepo() {
        List<Role> roles = Collections.singletonList(sampleRole);
        when(repo.findAll()).thenReturn(roles);

        List<Role> result = roleService.getAllRoles();

        assertThat(result).isSameAs(roles);
        verify(repo).findAll();
    }

    @Test
    void createRole_shouldIgnoreClientIdAndSave() {
        Role incoming = new Role();
        incoming.setId(99L);
        incoming.setName("USER");

        Role saved = new Role();
        saved.setId(2L);
        saved.setName("USER");

        when(repo.save(any(Role.class))).thenReturn(saved);

        Role result = roleService.createRole(incoming);

        ArgumentCaptor<Role> captor = ArgumentCaptor.forClass(Role.class);
        verify(repo).save(captor.capture());
        assertThat(captor.getValue().getId()).isNull();
        assertThat(result).isEqualTo(saved);
    }

    @Test
    void getRoleById_whenExists_shouldReturnRole() {
        when(repo.findById(1L)).thenReturn(Optional.of(sampleRole));

        Role result = roleService.getRoleById(1L);

        assertThat(result).isSameAs(sampleRole);
        verify(repo).findById(1L);
    }

    @Test
    void getRoleById_whenNotExists_shouldReturnNull() {
        when(repo.findById(42L)).thenReturn(Optional.empty());

        Role result = roleService.getRoleById(42L);

        assertThat(result).isNull();
        verify(repo).findById(42L);
    }

    @Test
    void updateRole_whenExists_shouldModifyAndSave() {
        Role payload = new Role();
        payload.setName("SUPER_ADMIN");

        when(repo.findById(1L)).thenReturn(Optional.of(sampleRole));
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Role result = roleService.updateRole(1L, payload);

        assertThat(result.getName()).isEqualTo("SUPER_ADMIN");
        verify(repo).save(sampleRole);
    }

    @Test
    void updateRole_whenNotExists_shouldReturnNull() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        Role result = roleService.updateRole(99L, new Role());

        assertThat(result).isNull();
        verify(repo, never()).save(any());
    }

    @Test
    void deleteRole_whenExists_shouldDeleteAndReturnTrue() {
        when(repo.existsById(1L)).thenReturn(true);

        boolean result = roleService.deleteRole(1L);

        assertThat(result).isTrue();
        verify(repo).deleteById(1L);
    }

    @Test
    void deleteRole_whenNotExists_shouldReturnFalse() {
        when(repo.existsById(50L)).thenReturn(false);

        boolean result = roleService.deleteRole(50L);

        assertThat(result).isFalse();
        verify(repo, never()).deleteById(anyLong());
    }
}
