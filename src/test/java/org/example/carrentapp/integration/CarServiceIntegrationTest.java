package org.example.carrentapp.integration;

import org.example.carrentapp.entity.Car;
import org.example.carrentapp.repository.CarRepository;
import org.example.carrentapp.service.CarService;
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

import static org.assertj.core.api.Assertions.*;

@Testcontainers
@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CarServiceIntegrationTest {

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

    @Autowired private CarService carService;
    @Autowired private CarRepository carRepository;

    @BeforeEach
    void setUp() {
        carRepository.deleteAll();
    }

    @Test
    void createAndGetAllCars() {
        Car c = new Car();
        c.setMake("Tesla"); c.setModel("Model S"); c.setYear(2022); c.setAvailable(true);
        Car created = carService.createCar(c);

        assertThat(created.getId()).isNotNull();
        var all = carService.getAllCars();
        assertThat(all).hasSize(1)
                .first()
                .extracting(Car::getMake, Car::getModel, Car::getYear, Car::getAvailable)  // Zamieniamy isAvailable() na getAvailable()
                .containsExactly("Tesla", "Model S", 2022, true);
    }

    @Test
    void findAvailableCars() {
        Car a = new Car(); a.setMake("A"); a.setModel("A1"); a.setYear(2020); a.setAvailable(true);
        Car b = new Car(); b.setMake("B"); b.setModel("B1"); b.setYear(2021); b.setAvailable(false);
        carService.createCar(a);
        carService.createCar(b);

        var avail = carService.findAvailableCars();
        assertThat(avail).hasSize(1)
                .first()
                .extracting(Car::getModel, Car::getAvailable)  // Zamieniamy isAvailable() na getAvailable()
                .containsExactly("A1", true);
    }

    @Test
    void getById_update_deleteExisting() {
        Car c = new Car();
        c.setMake("X"); c.setModel("Y"); c.setYear(2019); c.setAvailable(true);
        Car saved = carService.createCar(c);

        var fetched = carService.getCarById(saved.getId());
        assertThat(fetched).isNotNull().extracting(Car::getId).isEqualTo(saved.getId());

        Car payload = new Car();
        payload.setMake("Z"); payload.setModel("W"); payload.setYear(2018); payload.setAvailable(false);
        var updated = carService.updateCar(saved.getId(), payload);
        assertThat(updated).extracting(Car::getMake, Car::getModel, Car::getYear, Car::getAvailable)  // Zamieniamy isAvailable() na getAvailable()
                .containsExactly("Z", "W", 2018, false);

        boolean deleted = carService.deleteCar(saved.getId());
        assertThat(deleted).isTrue();
        assertThat(carService.getCarById(saved.getId())).isNull();
    }

    @Test
    void deleteCar_whenNotExists_returnsFalse() {
        // tutaj testujemy gałąź "return false"
        boolean deleted = carService.deleteCar(9999L);
        assertThat(deleted).isFalse();
    }
}
