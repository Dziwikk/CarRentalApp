package org.example.carrentapp.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.carrentapp.controller.RoleController;
import org.example.carrentapp.entity.Role;
import org.example.carrentapp.service.RoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class RoleControllerTest {

    @Mock private RoleService roleService;
    @InjectMocks private RoleController controller;

    private MockMvc mvc;
    private ObjectMapper mapper;
    private Role role;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
        mapper = new ObjectMapper();

        role = new Role();
        role.setId(3L);
        role.setName("MODERATOR");
    }

    @Test
    void getAllRoles_shouldReturnJsonArray() throws Exception {
        when(roleService.getAllRoles()).thenReturn(Collections.singletonList(role));

        mvc.perform(get("/api/roles"))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(Collections.singletonList(role))));
    }

    @Test
    void createRole_shouldReturnCreatedWithLocationHeader() throws Exception {
        Role toCreate = new Role();
        toCreate.setName("GUEST");

        Role created = new Role();
        created.setId(7L);
        created.setName("GUEST");

        when(roleService.createRole(any())).thenReturn(created);

        mvc.perform(post("/api/roles")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(toCreate)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/roles/7"))
                .andExpect(content().json(mapper.writeValueAsString(created)));
    }

    @Test
    void getRoleById_whenFound_shouldReturnRole() throws Exception {
        when(roleService.getRoleById(3L)).thenReturn(role);

        mvc.perform(get("/api/roles/3"))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(role)));
    }

    @Test
    void getRoleById_whenNotFound_shouldReturn404() throws Exception {
        when(roleService.getRoleById(99L)).thenReturn(null);

        mvc.perform(get("/api/roles/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateRole_whenFound_shouldReturnUpdated() throws Exception {
        Role payload = new Role();
        payload.setName("VIP");

        Role updated = new Role();
        updated.setId(3L);
        updated.setName("VIP");

        when(roleService.updateRole(eq(3L), any())).thenReturn(updated);

        mvc.perform(put("/api/roles/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(updated)));
    }

    @Test
    void updateRole_whenNotFound_shouldReturn404() throws Exception {
        when(roleService.updateRole(eq(123L), any())).thenReturn(null);

        mvc.perform(put("/api/roles/123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new Role())))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteRole_shouldAlwaysReturnOk() throws Exception {
        when(roleService.deleteRole(3L)).thenReturn(true);

        mvc.perform(delete("/api/roles/3"))
                .andExpect(status().isOk());
    }
}