package org.example.carrentapp.integration;

import org.example.carrentapp.entity.Car;
import org.example.carrentapp.repository.CarRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Testcontainers
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CarRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("test-db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",     postgres::getJdbcUrl);
        registry.add("spring.datasource.username",postgres::getUsername);
        registry.add("spring.datasource.password",postgres::getPassword);
    }

    @Autowired
    private CarRepository carRepository;

    @Test
    void findByAvailableTrue_whenNoCars_shouldReturnEmpty() {
        assertThat(carRepository.findByAvailableTrue()).isEmpty();
    }

    @Test
    void findByAvailableTrue_singleAvailableOnly() {
        Car a = new Car(); a.setMake("Toyota"); a.setModel("Corolla"); a.setYear(2020); a.setAvailable(true);
        Car b = new Car(); b.setMake("Ford");   b.setModel("Focus");   b.setYear(2018); b.setAvailable(false);
        carRepository.saveAll(List.of(a, b));

        List<Car> available = carRepository.findByAvailableTrue();
        assertThat(available)
                .hasSize(1)
                .allMatch(car -> car.getAvailable())  // Zamieniamy isAvailable() na getAvailable()
                .extracting(Car::getModel)
                .containsExactly("Corolla");
    }

    @Test
    void findByAvailableTrue_multipleAvailable_returnsAllTrue() {
        Car a = new Car(); a.setMake("Honda"); a.setModel("Civic");   a.setYear(2019); a.setAvailable(true);
        Car b = new Car(); b.setMake("BMW");   b.setModel("320i");    b.setYear(2021); b.setAvailable(false);
        Car c = new Car(); c.setMake("Audi");  c.setModel("A4");      c.setYear(2022); c.setAvailable(true);
        carRepository.saveAll(List.of(a, b, c));

        List<Car> available = carRepository.findByAvailableTrue();
        assertThat(available)
                .hasSize(2)
                .allMatch(car -> car.getAvailable())  // Zamieniamy isAvailable() na getAvailable()
                .extracting(Car::getModel)
                .containsExactlyInAnyOrder("Civic", "A4");
    }

    @Test
    void saveCar_withoutRequiredFields_throwsException() {
        Car incomplete = new Car();
        incomplete.setAvailable(true);
        // brak make, model (null) => NOT NULL violation
        assertThatThrownBy(() -> carRepository.saveAndFlush(incomplete))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
