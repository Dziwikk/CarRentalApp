// src/test/java/org/example/carrentapp/integration/ReservationServiceIntegrationTest.java
package org.example.carrentapp.integration;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Testcontainers
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReservationServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("test-db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url",     postgres::getJdbcUrl);
        r.add("spring.datasource.username",postgres::getUsername);
        r.add("spring.datasource.password",postgres::getPassword);
    }

    @Autowired private ReservationService reservationService;
    @Autowired private CarRepository        carRepo;
    @Autowired private UserRepository       userRepo;
    @Autowired private ReservationRepository resRepo;

    @BeforeEach
    void setUp() {
        resRepo.deleteAll();
        carRepo.deleteAll();
        userRepo.deleteAll();
    }

    @Test
    void createAndVerifyReservation() {
        Car car = new Car();
        car.setMake("M"); car.setModel("1"); car.setYear(2023); car.setAvailable(true);
        car = carRepo.save(car);

        User user = new User();
        user.setUsername("u1"); user.setPassword("p"); user.setEmail("u1@example.com");
        user = userRepo.save(user);

        ReservationDto dto = new ReservationDto();
        dto.setCarId(car.getId());
        dto.setUserId(user.getId());
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusDays(2));

        Long resId = reservationService.createReservation(dto);
        assertThat(resId).isNotNull();

        Reservation got = reservationService.getReservationById(resId);
        assertThat(got).isNotNull()
                .extracting(r -> r.getCar().getId(), r -> r.getUser().getId())
                .containsExactly(car.getId(), user.getId());

        Car updated = carRepo.findById(car.getId()).orElseThrow();
        assertThat(updated.isAvailable()).isFalse();
    }

    @Test
    void getAll_update_deleteExisting() {
        // utwórz dwie rezerwacje
        Car car1 = new Car(); car1.setMake("A"); car1.setModel("A1"); car1.setYear(2020); car1.setAvailable(true);
        car1 = carRepo.save(car1);
        User user1 = new User(); user1.setUsername("u2"); user1.setPassword("p"); user1.setEmail("u2@example.com");
        user1 = userRepo.save(user1);
        ReservationDto dto1 = new ReservationDto();
        dto1.setCarId(car1.getId());
        dto1.setUserId(user1.getId());
        dto1.setStartDate(LocalDate.now());
        dto1.setEndDate(LocalDate.now().plusDays(1));
        Long id1 = reservationService.createReservation(dto1);

        Car car2 = new Car(); car2.setMake("B"); car2.setModel("B1"); car2.setYear(2021); car2.setAvailable(true);
        car2 = carRepo.save(car2);
        User user2 = new User(); user2.setUsername("u3"); user2.setPassword("p"); user2.setEmail("u3@example.com");
        user2 = userRepo.save(user2);
        ReservationDto dto2 = new ReservationDto();
        dto2.setCarId(car2.getId());
        dto2.setUserId(user2.getId());
        dto2.setStartDate(LocalDate.now().plusDays(3));
        dto2.setEndDate(LocalDate.now().plusDays(5));
        Long id2 = reservationService.createReservation(dto2);

        // w bazie dwie
        List<Reservation> all = reservationService.getAllReservations();
        assertThat(all).hasSize(2)
                .extracting(Reservation::getId)
                .containsExactlyInAnyOrder(id1, id2);

        // update pierwszej
        Reservation first = reservationService.getReservationById(id1);
        ReservationDto upd = new ReservationDto();
        upd.setStartDate(first.getStartDate().plusDays(1));
        upd.setEndDate(first.getEndDate().plusDays(1));
        Reservation updated = reservationService.updateReservation(id1, upd);
        assertThat(updated).extracting(Reservation::getStartDate, Reservation::getEndDate)
                .containsExactly(upd.getStartDate(), upd.getEndDate());

        // usuń istniejącą
        boolean deleted = reservationService.deleteReservation(id1);
        assertThat(deleted).isTrue();
        assertThat(reservationService.getReservationById(id1)).isNull();
    }

    @Test
    void deleteReservation_whenNotExists_returnsFalse() {
        // testujemy gałąź "return false"
        boolean deleted = reservationService.deleteReservation(8888L);
        assertThat(deleted).isFalse();
    }
}
