package org.example.carrentapp.integration;

import org.example.carrentapp.entity.Car;
import org.example.carrentapp.repository.CarRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CarControllerIntegrationTest {

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
    @Autowired CarRepository carRepo;

    private String base;

    private TestRestTemplate userRest;
    private TestRestTemplate adminRest;

    @BeforeEach
    void setUp() {
        carRepo.deleteAll();
        // dodajmy dwa auta, jedno dostępne, drugie nie
        Car c1 = new Car(); c1.setMake("T"); c1.setModel("One"); c1.setYear(2020); c1.setAvailable(true);
        Car c2 = new Car(); c2.setMake("F"); c2.setModel("Two"); c2.setYear(2019); c2.setAvailable(false);
        carRepo.saveAll(List.of(c1, c2));

        base = "http://localhost:" + port + "/api/cars";

        userRest = rest.withBasicAuth("user", "password");
        adminRest = rest.withBasicAuth("admin", "password");
    }

    @Test
    void getAllCars_shouldReturnBoth() {
        ResponseEntity<Car[]> resp = userRest.getForEntity(base, Car[].class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).hasSize(2);
    }

    @Test
    void getAvailableCars_shouldReturnOnlyAvailable() {
        ResponseEntity<Car[]> resp = userRest.getForEntity(base + "/available", Car[].class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);

        Car[] avail = resp.getBody();
        assertThat(avail).hasSize(1);
        assertThat(avail[0].isAvailable()).isTrue();
        assertThat(avail[0].getMake()).isEqualTo("T");
    }

    @Test
    void getCarById_whenExists_shouldReturnCar() {
        Car saved = carRepo.findAll().stream().filter(Car::isAvailable).findFirst().orElseThrow();
        ResponseEntity<Car> resp = userRest.getForEntity(base + "/" + saved.getId(), Car.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody().getId()).isEqualTo(saved.getId());
    }

    @Test
    void getCarById_whenNotExists_shouldReturn404() {
        ResponseEntity<Car> resp = userRest.getForEntity(base + "/9999", Car.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void createUpdateDeleteFlow() {
        // (krótki sanity check istniejących testów)
        Car toCreate = new Car();
        toCreate.setMake("N"); toCreate.setModel("New"); toCreate.setYear(2021); toCreate.setAvailable(true);

        ResponseEntity<Car> post = adminRest.postForEntity(base, toCreate, Car.class);
        assertThat(post.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        Long id = post.getBody().getId();

        // UPDATE istniejącego
        Car payload = new Car();
        payload.setMake("U"); payload.setModel("Up"); payload.setYear(2022); payload.setAvailable(false);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Car> req = new HttpEntity<>(payload, headers);

        ResponseEntity<Car> put = adminRest.exchange(base + "/" + id, HttpMethod.PUT, req, Car.class);
        assertThat(put.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(put.getBody().getMake()).isEqualTo("U");

        // DELETE
        ResponseEntity<Void> del = adminRest.exchange(base + "/" + id, HttpMethod.DELETE, null, Void.class);
        assertThat(del.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void updateCar_whenNotExists_shouldReturn404() {
        Car payload = new Car();
        payload.setMake("Z"); payload.setModel("ZZ"); payload.setYear(2025); payload.setAvailable(true);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Car> req = new HttpEntity<>(payload, headers);

        ResponseEntity<Car> resp = adminRest.exchange(base + "/8888", HttpMethod.PUT, req, Car.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }
}
