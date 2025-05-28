// src/test/java/org/example/carrentapp/dto/ReservationDtoTest.java
package org.example.carrentapp.dto;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class ReservationDtoTest {

    @Test
    void testAllFieldsGetterSetter() {
        ReservationDto dto = new ReservationDto();
        LocalDate start = LocalDate.of(2025, 6, 1);
        LocalDate end   = LocalDate.of(2025, 6, 10);

        dto.setCarId(7L);
        dto.setUserId(13L);
        dto.setStartDate(start);
        dto.setEndDate(end);

        assertEquals(7L, dto.getCarId());
        assertEquals(13L, dto.getUserId());
        assertEquals(start, dto.getStartDate());
        assertEquals(end, dto.getEndDate());
    }
}
