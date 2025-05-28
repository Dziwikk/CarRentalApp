// src/test/java/org/example/carrentapp/dto/UserDtoTest.java
package org.example.carrentapp.unit;

import org.example.carrentapp.dto.UserDto;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserDtoTest {

    @Test
    void testAllFieldsGetterSetter() {
        UserDto dto = new UserDto();
        dto.setId(42L);
        dto.setUsername("alice");
        dto.setPassword("password");
        dto.setEmail("alice@example.com");

        assertEquals(42L, dto.getId());
        assertEquals("alice", dto.getUsername());
        assertEquals("password", dto.getPassword());
        assertEquals("alice@example.com", dto.getEmail());
    }
}
