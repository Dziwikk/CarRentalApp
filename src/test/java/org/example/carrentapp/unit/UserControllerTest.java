package org.example.carrentapp.all;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.carrentapp.controller.UserController;
import org.example.carrentapp.entity.User;
import org.example.carrentapp.service.UserService;
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

class UserControllerTest {

    @Mock private UserService userService;
    @InjectMocks private UserController controller;
    private MockMvc mvc;
    private ObjectMapper mapper;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mvc = MockMvcBuilders.standaloneSetup(controller).build();
        mapper = new ObjectMapper();

        user = new User();
        user.setId(5L);
        user.setUsername("dan");
        user.setEmail("dan@example.com");
    }

    @Test
    void getAllUsers_shouldReturnJsonArray() throws Exception {
        when(userService.getAllUsers()).thenReturn(Collections.singletonList(user));

        mvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(Collections.singletonList(user))));
    }

    @Test
    void createUser_shouldReturnCreatedWithLocationHeader() throws Exception {
        User toCreate = new User();
        toCreate.setUsername("eve");
        toCreate.setEmail("eve@example.com");

        User created = new User();
        created.setId(10L);
        created.setUsername("eve");
        created.setEmail("eve@example.com");

        when(userService.createUser(any())).thenReturn(created);

        mvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(toCreate)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/users/10"))
                .andExpect(content().json(mapper.writeValueAsString(created)));
    }

    @Test
    void getUserById_whenFound_shouldReturnUser() throws Exception {
        when(userService.getUserById(5L)).thenReturn(user);

        mvc.perform(get("/api/users/5"))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(user)));
    }

    @Test
    void getUserById_whenNotFound_shouldReturn404() throws Exception {
        when(userService.getUserById(99L)).thenReturn(null);

        mvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_whenFound_shouldReturnUpdated() throws Exception {
        User payload = new User();
        payload.setUsername("frank");
        payload.setEmail("frank@example.com");

        User updated = new User();
        updated.setId(5L);
        updated.setUsername("frank");
        updated.setEmail("frank@example.com");

        when(userService.updateUser(eq(5L), any())).thenReturn(updated);

        mvc.perform(put("/api/users/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(updated)));
    }

    @Test
    void updateUser_whenNotFound_shouldReturn404() throws Exception {
        when(userService.updateUser(eq(123L), any())).thenReturn(null);

        mvc.perform(put("/api/users/123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new User())))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_shouldAlwaysReturnOk() throws Exception {
        // Correctly stub the boolean-returning method:
        when(userService.deleteUser(5L)).thenReturn(true);

        mvc.perform(delete("/api/users/5"))
                .andExpect(status().isOk());
    }
}
