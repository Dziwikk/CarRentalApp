package org.example.carrentapp.unit;

import org.example.carrentapp.entity.Car;
import org.example.carrentapp.repository.CarRepository;
import org.example.carrentapp.service.CarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CarServiceTest {

    @Mock
    private CarRepository repo;

    @InjectMocks
    private CarService carService;

    private Car sampleCar;

    private Car a;
    private Car b;

    @BeforeEach
    void setUp() {
        sampleCar = new Car();
        sampleCar.setId(1L);
        sampleCar.setMake("Toyota");
        sampleCar.setModel("Corolla");
        sampleCar.setYear(2020);
        sampleCar.setAvailable(false);  // Ustawiamy dostępność na true

        // Przygotowanie innych samochodów, np. Audi
        Car a = new Car();
        a.setMake("Audi");
        a.setModel("A3");
        a.setYear(2021);
        a.setAvailable(false);  // Samochód dostępny

        Car b = new Car();
        b.setMake("Ford");
        b.setModel("Focus");
        b.setYear(2020);
        b.setAvailable(false);  // Samochód niedostępny
    }

    @Test
    void getAllCars_shouldReturnListFromRepo() {
        List<Car> cars = Collections.singletonList(sampleCar);
        when(repo.findAll()).thenReturn(cars);

        List<Car> result = carService.getAllCars();

        assertThat(result).isSameAs(cars);
        verify(repo).findAll();
    }







    @Test
    void createCar_shouldSaveAndReturn() {
        Car newCar = new Car();
        newCar.setMake("Honda");
        newCar.setModel("Civic");
        newCar.setYear(2021);
        newCar.setAvailable(false);

        Car saved = new Car();
        saved.setId(2L);
        saved.setMake("Honda");
        saved.setModel("Civic");
        saved.setYear(2021);
        saved.setAvailable(false);

        when(repo.save(newCar)).thenReturn(saved);

        Car result = carService.createCar(newCar);

        assertThat(result).isEqualTo(saved);
        verify(repo).save(newCar);
    }

    @Test
    void getCarById_whenExists_shouldReturnCar() {
        when(repo.findById(1L)).thenReturn(Optional.of(sampleCar));

        Car result = carService.getCarById(1L);

        assertThat(result).isSameAs(sampleCar);
        verify(repo).findById(1L);
    }

    @Test
    void getCarById_whenNotExists_shouldReturnNull() {
        when(repo.findById(99L)).thenReturn(Optional.empty());

        Car result = carService.getCarById(99L);

        assertThat(result).isNull();
        verify(repo).findById(99L);
    }

    @Test
    void updateCar_whenExists_shouldModifyAndSave() {
        Car payload = new Car();
        payload.setMake("Ford");
        payload.setModel("Focus");
        payload.setYear(2019);
        payload.setAvailable(false);

        when(repo.findById(1L)).thenReturn(Optional.of(sampleCar));
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        Car result = carService.updateCar(1L, payload);

        assertThat(result.getMake()).isEqualTo("Ford");
        assertThat(result.getModel()).isEqualTo("Focus");
        assertThat(result.getYear()).isEqualTo(2019);
        assertThat(result.getAvailable()).isFalse();  // Używamy getAvailable() zamiast isAvailable()
        verify(repo).save(sampleCar);
    }

    @Test
    void updateCar_whenNotExists_shouldReturnNull() {
        when(repo.findById(50L)).thenReturn(Optional.empty());

        Car result = carService.updateCar(50L, new Car());

        assertThat(result).isNull();
        verify(repo, never()).save(any());
    }

    @Test
    void deleteCar_whenExists_shouldDeleteAndReturnTrue() {
        when(repo.existsById(1L)).thenReturn(true);

        boolean result = carService.deleteCar(1L);

        assertThat(result).isTrue();
        verify(repo).deleteById(1L);
    }

    @Test
    void deleteCar_whenNotExists_shouldReturnFalse() {
        when(repo.existsById(100L)).thenReturn(false);

        boolean result = carService.deleteCar(100L);

        assertThat(result).isFalse();
        verify(repo, never()).deleteById(anyLong());
    }
}
