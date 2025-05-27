// src/test/java/org/example/carrentapp/unit/ReservationServiceTest.java
package org.example.carrentapp.unit;

import jakarta.persistence.EntityNotFoundException;
import org.example.carrentapp.dto.ReservationDto;
import org.example.carrentapp.entity.Car;
import org.example.carrentapp.entity.Reservation;
import org.example.carrentapp.entity.User;
import org.example.carrentapp.repository.CarRepository;
import org.example.carrentapp.repository.ReservationRepository;
import org.example.carrentapp.repository.UserRepository;
import org.example.carrentapp.service.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @Mock
    private ReservationRepository reservationRepo;

    @Mock
    private CarRepository carRepo;

    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private ReservationService reservationService;

    private Car sampleCar;
    private User sampleUser;
    private Reservation sampleReservation;

    @BeforeEach
    void setUp() {
        // przygotuj encje Car
        sampleCar = new Car();
        sampleCar.setId(1L);
        sampleCar.setAvailable(true);

        // przygotuj encję User
        sampleUser = new User();
        sampleUser.setId(2L);
        sampleUser.setUsername("jan");
        sampleUser.setEmail("jan@example.com");
        sampleUser.setPassword("pwd");

        // przygotuj encję Reservation
        sampleReservation = new Reservation();
        sampleReservation.setId(3L);
        sampleReservation.setCar(sampleCar);
        sampleReservation.setUser(sampleUser);
        sampleReservation.setStartDate(LocalDate.of(2025, 6, 1));
        sampleReservation.setEndDate(LocalDate.of(2025, 6, 10));
    }

    @Test
    void createReservation_shouldSaveAndReturnId() {
        // given
        ReservationDto dto = new ReservationDto();
        dto.setCarId(sampleCar.getId());
        dto.setUserId(sampleUser.getId());
        dto.setStartDate(sampleReservation.getStartDate());
        dto.setEndDate(sampleReservation.getEndDate());

        when(carRepo.findById(dto.getCarId())).thenReturn(Optional.of(sampleCar));
        when(userRepo.findById(dto.getUserId())).thenReturn(Optional.of(sampleUser));
        // service ustawi available=false i zapisze car
        when(carRepo.save(sampleCar)).thenReturn(sampleCar);
        // repo.save zwraca sampleReservation
        when(reservationRepo.save(any(Reservation.class))).thenReturn(sampleReservation);

        // when
        Long result = reservationService.createReservation(dto);

        // then
        assertThat(result).isEqualTo(sampleReservation.getId());
        assertThat(sampleCar.isAvailable()).isFalse();
        verify(carRepo).save(sampleCar);
        verify(reservationRepo).save(any(Reservation.class));
    }

    @Test
    void createReservation_carNotFound_shouldThrow() {
        ReservationDto dto = new ReservationDto();
        dto.setCarId(99L);
        dto.setUserId(sampleUser.getId());

        when(carRepo.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.createReservation(dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Car not found");
    }

    @Test
    void createReservation_userNotFound_shouldThrow() {
        ReservationDto dto = new ReservationDto();
        dto.setCarId(sampleCar.getId());
        dto.setUserId(42L);

        when(carRepo.findById(dto.getCarId())).thenReturn(Optional.of(sampleCar));
        when(userRepo.findById(42L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reservationService.createReservation(dto))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void createReservation_carNotAvailable_shouldThrow() {
        sampleCar.setAvailable(false);

        ReservationDto dto = new ReservationDto();
        dto.setCarId(sampleCar.getId());
        dto.setUserId(sampleUser.getId());

        when(carRepo.findById(dto.getCarId())).thenReturn(Optional.of(sampleCar));


        assertThatThrownBy(() -> reservationService.createReservation(dto))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Car is already reserved");
    }

    @Test
    void getAllReservations_shouldReturnListFromRepo() {
        List<Reservation> list = Collections.singletonList(sampleReservation);
        when(reservationRepo.findAll()).thenReturn(list);

        List<Reservation> result = reservationService.getAllReservations();

        assertThat(result).isSameAs(list);
        verify(reservationRepo).findAll();
    }

    @Test
    void getReservationById_whenExists_shouldReturn() {
        when(reservationRepo.findById(3L)).thenReturn(Optional.of(sampleReservation));

        Reservation result = reservationService.getReservationById(3L);

        assertThat(result).isSameAs(sampleReservation);
        verify(reservationRepo).findById(3L);
    }

    @Test
    void getReservationById_whenNotExists_shouldReturnNull() {
        when(reservationRepo.findById(50L)).thenReturn(Optional.empty());

        Reservation result = reservationService.getReservationById(50L);

        assertThat(result).isNull();
        verify(reservationRepo).findById(50L);
    }

    @Test
    void updateReservation_whenExists_shouldModifyDatesAndSave() {
        ReservationDto dto = new ReservationDto();
        dto.setStartDate(LocalDate.of(2025, 7, 1));
        dto.setEndDate(LocalDate.of(2025, 7, 5));

        when(reservationRepo.findById(3L)).thenReturn(Optional.of(sampleReservation));
        when(reservationRepo.save(sampleReservation)).thenReturn(sampleReservation);

        Reservation result = reservationService.updateReservation(3L, dto);

        assertThat(result.getStartDate()).isEqualTo(dto.getStartDate());
        assertThat(result.getEndDate()).isEqualTo(dto.getEndDate());
        verify(reservationRepo).save(sampleReservation);
    }

    @Test
    void updateReservation_whenNotExists_shouldReturnNull() {
        ReservationDto dto = new ReservationDto();
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusDays(1));

        when(reservationRepo.findById(99L)).thenReturn(Optional.empty());

        Reservation result = reservationService.updateReservation(99L, dto);

        assertThat(result).isNull();
        verify(reservationRepo, never()).save(any());
    }

    @Test
    void deleteReservation_whenExists_shouldDeleteAndReturnTrue() {
        when(reservationRepo.existsById(3L)).thenReturn(true);

        boolean deleted = reservationService.deleteReservation(3L);

        assertThat(deleted).isTrue();
        verify(reservationRepo).deleteById(3L);
    }

    @Test
    void deleteReservation_whenNotExists_shouldReturnFalse() {
        when(reservationRepo.existsById(100L)).thenReturn(false);

        boolean deleted = reservationService.deleteReservation(100L);

        assertThat(deleted).isFalse();
        verify(reservationRepo, never()).deleteById(anyLong());
    }
}
