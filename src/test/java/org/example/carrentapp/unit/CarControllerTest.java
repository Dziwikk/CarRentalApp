// src/test/java/org/example/carrentapp/unit/CarControllerTest.java
package org.example.carrentapp.unit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.carrentapp.controller.CarController;
import org.example.carrentapp.entity.Car;
import org.example.carrentapp.service.CarService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CarControllerTest {

    @Mock private CarService carService;
    @InjectMocks private CarController controller;

    private MockMvc mvc;
    private ObjectMapper mapper;
    private Car car;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mapper = new ObjectMapper();
        mvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter(mapper))
                .build();

        car = new Car();
        car.setId(5L);
        car.setMake("BMW");
        car.setModel("X5");
        car.setYear(2022);
        car.setAvailable(true);
    }

    @Test
    void getAllCars_shouldReturnJsonArray() throws Exception {
        when(carService.getAllCars()).thenReturn(Collections.singletonList(car));

        mvc.perform(get("/api/cars"))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(Collections.singletonList(car))));
    }

    @Test
    void getAvailableCars_shouldReturnJsonArray() throws Exception {
        // Przygotowanie dwóch samochodów
        Car a = new Car();
        a.setId(6L);
        a.setMake("Audi");
        a.setModel("A3");
        a.setYear(2021);
        a.setAvailable(true);

        when(carService.findAvailableCars()).thenReturn(Arrays.asList(car, a));  // Używamy mockowanego wyniku

        // Wykonanie zapytania i porównanie wyników
        mvc.perform(get("/api/cars/available"))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(Arrays.asList(car, a))));  // Używamy poprawnego formatu JSON
    }


    @Test
    void createCar_shouldReturnCreatedWithLocationHeader() throws Exception {
        Car toCreate = new Car();
        toCreate.setMake("Audi");
        toCreate.setModel("A4");
        toCreate.setYear(2023);
        toCreate.setAvailable(false);

        Car created = new Car();
        created.setId(10L);
        created.setMake("Audi");
        created.setModel("A4");
        created.setYear(2023);
        created.setAvailable(false);

        when(carService.createCar(any())).thenReturn(created);

        mvc.perform(post("/api/cars")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(toCreate)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/cars/10"))
                .andExpect(content().json(mapper.writeValueAsString(created)));
    }

    @Test
    void getCarById_whenFound_shouldReturnCar() throws Exception {
        when(carService.getCarById(5L)).thenReturn(car);

        mvc.perform(get("/api/cars/5"))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(car)));
    }

    @Test
    void getCarById_whenNotFound_shouldReturn404() throws Exception {
        when(carService.getCarById(99L)).thenReturn(null);

        mvc.perform(get("/api/cars/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateCar_whenFound_shouldReturnUpdated() throws Exception {
        Car payload = new Car();
        payload.setMake("Tesla");
        payload.setModel("Model 3");
        payload.setYear(2024);
        payload.setAvailable(true);

        Car updated = new Car();
        updated.setId(5L);
        updated.setMake("Tesla");
        updated.setModel("Model 3");
        updated.setYear(2024);
        updated.setAvailable(true);

        when(carService.updateCar(eq(5L), any())).thenReturn(updated);

        mvc.perform(put("/api/cars/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(updated)));
    }

    @Test
    void updateCar_whenNotFound_shouldReturn404() throws Exception {
        when(carService.updateCar(eq(123L), any())).thenReturn(null);

        mvc.perform(put("/api/cars/123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new Car())))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCar_shouldAlwaysReturnOk() throws Exception {
        when(carService.deleteCar(5L)).thenReturn(true);

        mvc.perform(delete("/api/cars/5"))
                .andExpect(status().isOk());
    }
}
