// src/test/java/org/example/carrentapp/controller/ReservationControllerTest.java
package org.example.carrentapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.example.carrentapp.dto.ReservationDto;
import org.example.carrentapp.entity.Reservation;
import org.example.carrentapp.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ReservationControllerTest {

    @Mock
    private ReservationService reservationService;

    @InjectMocks
    private ReservationController controller;

    private MockMvc mvc;
    private ObjectMapper mapper;
    private Reservation sampleReservation;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // ObjectMapper z modułem do LocalDate
        mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());

        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper))
                .build();

        // przykładowa encja do GET-ów
        sampleReservation = new Reservation();
        sampleReservation.setId(3L);
        sampleReservation.setStartDate(LocalDate.of(2025, 9, 10));
        sampleReservation.setEndDate(LocalDate.of(2025, 9, 20));
    }

    @Test
    void getAllReservations_shouldReturnJsonArray() throws Exception {
        when(reservationService.getAllReservations())
                .thenReturn(Collections.singletonList(sampleReservation));

        mvc.perform(get("/api/reservations"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        mapper.writeValueAsString(Collections.singletonList(sampleReservation))
                ));
    }

    @Test
    void createReservation_shouldReturnCreatedWithLocationHeader() throws Exception {
        // przygotuj DTO do wysłania
        ReservationDto dto = new ReservationDto();
        dto.setCarId(5L);
        dto.setUserId(7L);
        dto.setStartDate(LocalDate.of(2025, 10, 1));
        dto.setEndDate(LocalDate.of(2025, 10, 5));

        // stub: usługa zwraca nowy ID
        when(reservationService.createReservation(any(ReservationDto.class)))
                .thenReturn(8L);

        mvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/reservations/8"))
                // brak body przy create
                .andExpect(content().string(""));
    }

    @Test
    void getReservationById_whenFound_shouldReturnReservation() throws Exception {
        when(reservationService.getReservationById(3L))
                .thenReturn(sampleReservation);

        mvc.perform(get("/api/reservations/3"))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(sampleReservation)));
    }

    @Test
    void getReservationById_whenNotFound_shouldReturn404() throws Exception {
        when(reservationService.getReservationById(99L))
                .thenReturn(null);

        mvc.perform(get("/api/reservations/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateReservation_whenFound_shouldReturnUpdated() throws Exception {
        // przygotuj oczekiwany wynik
        Reservation updated = new Reservation();
        updated.setId(3L);
        updated.setStartDate(LocalDate.of(2025, 11, 1));
        updated.setEndDate(LocalDate.of(2025, 11, 7));

        // stub serwisu: przy poprawnym id i dowolnym DTO zwracamy obiekt
        when(reservationService.updateReservation(
                eq(3L),
                any(ReservationDto.class)))
                .thenReturn(updated);

        // payload jako DTO
        ReservationDto payload = new ReservationDto();
        payload.setStartDate(updated.getStartDate());
        payload.setEndDate(updated.getEndDate());

        mvc.perform(put("/api/reservations/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(updated)));
    }

    @Test
    void updateReservation_whenNotFound_shouldReturn404() throws Exception {
        when(reservationService.updateReservation(
                eq(123L),
                any(ReservationDto.class)))
                .thenReturn(null);

        mvc.perform(put("/api/reservations/123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new ReservationDto())))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteReservation_whenDeleted_shouldReturnOk() throws Exception {
        when(reservationService.deleteReservation(3L)).thenReturn(true);

        mvc.perform(delete("/api/reservations/3"))
                .andExpect(status().isOk());
    }

    @Test
    void deleteReservation_whenNotDeleted_shouldReturn404() throws Exception {
        when(reservationService.deleteReservation(99L)).thenReturn(false);

        mvc.perform(delete("/api/reservations/99"))
                .andExpect(status().isNotFound());
    }
}
