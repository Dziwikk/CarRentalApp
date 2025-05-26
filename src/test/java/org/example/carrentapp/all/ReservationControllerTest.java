// src/test/java/org/example/carrentapp/controller/ReservationControllerTest.java
package org.example.carrentapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

    @Mock private ReservationService reservationService;
    @InjectMocks private ReservationController controller;

    private MockMvc mvc;
    private ObjectMapper mapper;
    private Reservation reservation;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        // Prepare an ObjectMapper that knows about Java 8 dates
        mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule());

        // Register it with MockMvc
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper))
                .build();

        reservation = new Reservation();
        reservation.setId(3L);
        reservation.setStartDate(LocalDate.of(2025, 9, 10));
        reservation.setEndDate(LocalDate.of(2025, 9, 20));
    }

    @Test
    void getAllReservations_shouldReturnJsonArray() throws Exception {
        when(reservationService.getAllReservations())
                .thenReturn(Collections.singletonList(reservation));

        mvc.perform(get("/api/reservations"))
                .andExpect(status().isOk())
                .andExpect(content().json(
                        mapper.writeValueAsString(Collections.singletonList(reservation))
                ));
    }

    @Test
    void createReservation_shouldReturnCreatedWithLocationHeader() throws Exception {
        Reservation toCreate = new Reservation();
        toCreate.setStartDate(LocalDate.of(2025, 10, 1));
        toCreate.setEndDate(LocalDate.of(2025, 10, 5));

        Reservation created = new Reservation();
        created.setId(8L);
        created.setStartDate(toCreate.getStartDate());
        created.setEndDate(toCreate.getEndDate());

        when(reservationService.createReservation(any())).thenReturn(created);

        mvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(toCreate)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/reservations/8"))
                .andExpect(content().json(mapper.writeValueAsString(created)));
    }

    @Test
    void getReservationById_whenFound_shouldReturnReservation() throws Exception {
        when(reservationService.getReservationById(3L)).thenReturn(reservation);

        mvc.perform(get("/api/reservations/3"))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(reservation)));
    }

    @Test
    void getReservationById_whenNotFound_shouldReturn404() throws Exception {
        when(reservationService.getReservationById(99L)).thenReturn(null);

        mvc.perform(get("/api/reservations/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateReservation_whenFound_shouldReturnUpdated() throws Exception {
        Reservation updated = new Reservation();
        updated.setId(3L);
        updated.setStartDate(LocalDate.of(2025, 11, 1));
        updated.setEndDate(LocalDate.of(2025, 11, 7));

        // Stub for any payload matching a Reservation
        when(reservationService.updateReservation(
                eq(3L),
                any(Reservation.class)))
                .thenReturn(updated);

        // Build a JSON body for the update
        Reservation payload = new Reservation();
        payload.setStartDate(LocalDate.of(2025, 11, 1));
        payload.setEndDate(LocalDate.of(2025, 11, 7));

        mvc.perform(put("/api/reservations/3")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(updated)));
    }

    @Test
    void updateReservation_whenNotFound_shouldReturn404() throws Exception {
        // Use eq() for the Long and any(Reservation.class) for the payload:
        when(reservationService.updateReservation(
                eq(123L),
                any(Reservation.class)))
                .thenReturn(null);

        mvc.perform(put("/api/reservations/123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new Reservation())))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteReservation_shouldAlwaysReturnOk() throws Exception {
        when(reservationService.deleteReservation(3L)).thenReturn(true);

        mvc.perform(delete("/api/reservations/3"))
                .andExpect(status().isOk());
    }
}
