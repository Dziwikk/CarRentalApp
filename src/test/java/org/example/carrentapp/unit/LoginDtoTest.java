// src/test/java/org/example/carrentapp/dto/LoginDtoTest.java
package org.example.carrentapp.unit;

import org.example.carrentapp.dto.LoginDto;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class LoginDtoTest {

    @Test
    void testUsernameGetterSetter() {
        LoginDto dto = new LoginDto();
        dto.setUsername("user123");
        assertEquals("user123", dto.getUsername());
    }

    @Test
    void testPasswordGetterSetter() {
        LoginDto dto = new LoginDto();
        dto.setPassword("s3cr3t");
        assertEquals("s3cr3t", dto.getPassword());
    }
}
