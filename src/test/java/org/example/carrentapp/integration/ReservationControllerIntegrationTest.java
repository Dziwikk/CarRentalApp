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
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ReservationControllerIntegrationTest {

    @Container
    static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("test-db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url",     pg::getJdbcUrl);
        r.add("spring.datasource.username",pg::getUsername);
        r.add("spring.datasource.password",pg::getPassword);
    }

    @LocalServerPort int port;
    @Autowired TestRestTemplate rest;
    @Autowired UserRepository userRepo;
    @Autowired CarRepository carRepo;
    @Autowired ReservationRepository resRepo;

    private String resBase;
    private TestRestTemplate adminRest;
    private Long userId;
    private Long carId;

    @BeforeEach
    void setUp() {
        resRepo.deleteAll();
        carRepo.deleteAll();
        userRepo.deleteAll();
        resBase = "http://localhost:" + port + "/api/reservations";
        adminRest = rest.withBasicAuth("admin", "password");

        // create user and car
        User user = new User();
        user.setUsername("u1");
        user.setPassword("pw");
        user.setEmail("u1@example.com");
        userId = userRepo.save(user).getId();

        Car car = new Car();
        car.setMake("Mk");
        car.setModel("Md");
        car.setYear(2023);
        car.setAvailable(true);
        carId = carRepo.save(car).getId();
    }

    @Test
    void crudReservation_shouldCoverAllFields() {
        // LIST empty
        ResponseEntity<Reservation[]> empty = adminRest.getForEntity(resBase, Reservation[].class);
        assertThat(empty.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(empty.getBody()).isEmpty();

        // CREATE payload DTO
        ReservationDto toCreate = new ReservationDto();
        toCreate.setCarId(carId);
        toCreate.setUserId(userId);
        toCreate.setStartDate(LocalDate.now());
        toCreate.setEndDate(LocalDate.now().plusDays(2));

        // POST -> 201 Created + Location
        ResponseEntity<Void> createResponse = adminRest.postForEntity(resBase, toCreate, Void.class);
        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        URI location = createResponse.getHeaders().getLocation();
        assertThat(location).isNotNull();

        // GET created Reservation entity
        ResponseEntity<Reservation> createdGet = adminRest.getForEntity(location, Reservation.class);
        assertThat(createdGet.getStatusCode()).isEqualTo(HttpStatus.OK);
        Reservation created = createdGet.getBody();
        assertThat(created).isNotNull();
        assertThat(created.getId()).isNotNull();
        assertThat(created.getCar().getId()).isEqualTo(carId);
        assertThat(created.getUser().getId()).isEqualTo(userId);
        assertThat(created.getStartDate()).isEqualTo(toCreate.getStartDate());
        assertThat(created.getEndDate()).isEqualTo(toCreate.getEndDate());

        Long id = created.getId();

        // GET by ID via base URI
        ResponseEntity<Reservation> get = adminRest.getForEntity(resBase + "/" + id, Reservation.class);
        assertThat(get.getStatusCode()).isEqualTo(HttpStatus.OK);
        // compare all fields recursively instead of relying on object identity
        assertThat(get.getBody())
                .usingRecursiveComparison()
                .isEqualTo(created);

        // UPDATE dates
        ReservationDto upd = new ReservationDto();
        upd.setStartDate(toCreate.getStartDate().plusDays(1));
        upd.setEndDate(toCreate.getEndDate().plusDays(1));
        HttpHeaders headers = new HttpHeaders(); headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<ReservationDto> req = new HttpEntity<>(upd, headers);

        ResponseEntity<Reservation> put = adminRest.exchange(resBase + "/" + id, HttpMethod.PUT, req, Reservation.class);
        assertThat(put.getStatusCode()).isEqualTo(HttpStatus.OK);
        Reservation updated = put.getBody();
        assertThat(updated.getStartDate()).isEqualTo(upd.getStartDate());
        assertThat(updated.getEndDate()).isEqualTo(upd.getEndDate());

        // DELETE
        ResponseEntity<Void> del = adminRest.exchange(resBase + "/" + id, HttpMethod.DELETE, null, Void.class);
        assertThat(del.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(adminRest.getForEntity(resBase + "/" + id, Reservation.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }
}
