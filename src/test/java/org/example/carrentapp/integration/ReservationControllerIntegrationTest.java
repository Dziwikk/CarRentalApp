package org.example.carrentapp.integration;

import org.example.carrentapp.dto.ReservationDto;
import org.example.carrentapp.entity.Car;
import org.example.carrentapp.entity.Reservation;
import org.example.carrentapp.entity.User;
import org.example.carrentapp.repository.CarRepository;
import org.example.carrentapp.repository.ReservationRepository;
import org.example.carrentapp.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.URI;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReservationControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("test-db").withUsername("test").withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url",     pg::getJdbcUrl);
        r.add("spring.datasource.username",pg::getUsername);
        r.add("spring.datasource.password",pg::getPassword);
    }

    @LocalServerPort int port;
    @Autowired TestRestTemplate rest;
    @Autowired CarRepository carRepo;
    @Autowired UserRepository userRepo;
    @Autowired ReservationRepository resRepo;

    private String base;
    private TestRestTemplate userRest;
    private TestRestTemplate adminRest;

    @BeforeEach
    void setUp() {
        resRepo.deleteAll();
        carRepo.deleteAll();
        userRepo.deleteAll();
        base = "http://localhost:" + port + "/api/reservations";
        userRest = rest.withBasicAuth("user", "password");
        adminRest = rest.withBasicAuth("admin", "password");
    }

    @Test
    void emptyList_thenCrudReservation() {
        // LIST empty
        ResponseEntity<Reservation[]> empty = userRest.getForEntity(base, Reservation[].class);
        assertThat(empty.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(empty.getBody()).isEmpty();

        // PREPARE CAR
        Car carEntity = new Car();
        carEntity.setMake("Mk");
        carEntity.setModel("Md");
        carEntity.setYear(2023);
        carEntity.setAvailable(true);
        Car car = carRepo.save(carEntity);

        // PREPARE USER
        User userEntity = new User();
        userEntity.setUsername("u1");
        userEntity.setPassword("pw");
        userEntity.setEmail("u1@e");
        User user = userRepo.save(userEntity);

        // CREATE
        ReservationDto dto = new ReservationDto();
        dto.setCarId(car.getId());
        dto.setUserId(user.getId());
        dto.setStartDate(LocalDate.now());
        dto.setEndDate(LocalDate.now().plusDays(2));

        ResponseEntity<Void> createResp = userRest.postForEntity(base, dto, Void.class);
        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        URI loc = createResp.getHeaders().getLocation();
        assertThat(loc).isNotNull();
        Long id = Long.parseLong(loc.getPath().replaceAll(".*/(\\d+)$","$1"));

        // GET by id
        ResponseEntity<Reservation> get = userRest.getForEntity(base + "/" + id, Reservation.class);
        assertThat(get.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(get.getBody().getId()).isEqualTo(id);

        // UPDATE
        ReservationDto upd = new ReservationDto();
        upd.setStartDate(dto.getStartDate().plusDays(1));
        upd.setEndDate(dto.getEndDate().plusDays(1));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ReservationDto> req = new HttpEntity<>(upd, headers);
        ResponseEntity<Reservation> put = userRest.exchange(base + "/" + id, HttpMethod.PUT, req, Reservation.class);
        assertThat(put.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(put.getBody())
                .extracting(Reservation::getStartDate, Reservation::getEndDate)
                .containsExactly(upd.getStartDate(), upd.getEndDate());

        // DELETE
        ResponseEntity<Void> del = userRest.exchange(base + "/" + id, HttpMethod.DELETE, null, Void.class);
        assertThat(del.getStatusCode()).isEqualTo(HttpStatus.OK);

        // after delete GET returns 404
        assertThat(userRest.getForEntity(base + "/" + id, Reservation.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void updateReservation_whenNotExists_shouldReturn404() {
        ReservationDto dummy = new ReservationDto();
        dummy.setStartDate(LocalDate.now());
        dummy.setEndDate(LocalDate.now().plusDays(1));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ReservationDto> req = new HttpEntity<>(dummy, headers);

        ResponseEntity<Reservation> resp = userRest.exchange(base + "/9999", HttpMethod.PUT, req, Reservation.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteReservation_whenNotExists_shouldReturn404() {
        ResponseEntity<Void> resp = userRest.exchange(base + "/8888", HttpMethod.DELETE, null, Void.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
