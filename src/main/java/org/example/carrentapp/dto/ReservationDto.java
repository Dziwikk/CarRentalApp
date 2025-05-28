// src/main/java/org/example/carrentapp/dto/ReservationDto.java
package org.example.carrentapp.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor
public class ReservationDto {
    private Long id;        // <-- dodaj to
    private Long carId;
    private Long userId;
    private LocalDate startDate;
    private LocalDate endDate;
}
