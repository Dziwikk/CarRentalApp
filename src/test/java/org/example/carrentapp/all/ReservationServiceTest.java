package org.example.carrentapp.all;

import org.example.carrentapp.entity.Reservation;
import org.example.carrentapp.repository.ReservationRepository;
import org.example.carrentapp.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository repo;

    @InjectMocks
    private ReservationService reservationService;

    private Reservation sample;

    @BeforeEach
    void setUp() {
        sample = new Reservation();
        sample.setId(1L);
        sample.setStartDate(LocalDate.of(2025, 6, 1));
        sample.setEndDate(LocalDate.of(2025, 6, 10));
    }

    @Test
    void getAllReservations_shouldReturnListFromRepo() {
        List<Reservation> list = Collections.singletonList(sample);
        when(repo.findAll()).thenReturn(list);

        List<Reservation> result = reservationService.getAllReservations();

        assertThat(result).isSameAs(list);
        verify(repo).findAll();
    }

    @Test
    void createReservation_shouldSaveAndReturn() {
        Reservation toCreate = new Reservation();
        toCreate.setStartDate(LocalDate.of(2025, 7, 1));
        toCreate.setEndDate(LocalDate.of(2025, 7, 5));

        Reservation saved = new Reservation();
        saved.setId(2L);
        saved.setStartDate(toCreate.getStartDate());
        saved.setEndDate(toCreate.getEndDate());

        when(repo.save(toCreate)).thenReturn(saved);

        Reservation result = reservationService.createReservation(toCreate);

        assertThat(result).isEqualTo(saved);
        verify(repo).save(toCreate);
    }

    @Test
    void getReservationById_whenExists_shouldReturn() {
        when(repo.findById(1L)).thenReturn(Optional.of(sample));

        Reservation result = reservationService.getReservationById(1L);

        assertThat(result).isSameAs(sample);
        verify(repo).findById(1L);
    }

    @Test
    void getReservationById_whenNotExists_shouldReturnNull() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        Reservation result = reservationService.getReservationById(99L);

        assertThat(result).isNull();
        verify(repo).findById(99L);
    }

    @Test
    void updateReservation_whenExists_shouldModifyAndSave() {
        Reservation payload = new Reservation();
        payload.setStartDate(LocalDate.of(2025, 8, 1));
        payload.setEndDate(LocalDate.of(2025, 8, 15));

        when(repo.findById(1L)).thenReturn(Optional.of(sample));
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Reservation result = reservationService.updateReservation(1L, payload);

        assertThat(result.getStartDate()).isEqualTo(payload.getStartDate());
        assertThat(result.getEndDate()).isEqualTo(payload.getEndDate());
        verify(repo).save(sample);
    }

    @Test
    void updateReservation_whenNotExists_shouldReturnNull() {
        when(repo.findById(50L)).thenReturn(Optional.empty());

        Reservation result = reservationService.updateReservation(50L, new Reservation());

        assertThat(result).isNull();
        verify(repo, never()).save(any());
    }

    @Test
    void deleteReservation_whenExists_shouldDeleteAndReturnTrue() {
        when(repo.existsById(1L)).thenReturn(true);

        boolean result = reservationService.deleteReservation(1L);

        assertThat(result).isTrue();
        verify(repo).deleteById(1L);
    }

    @Test
    void deleteReservation_whenNotExists_shouldReturnFalse() {
        when(repo.existsById(100L)).thenReturn(false);

        boolean result = reservationService.deleteReservation(100L);

        assertThat(result).isFalse();
        verify(repo, never()).deleteById(anyLong());
    }
}
