package org.example.carrentapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.carrentapp.entity.Car;
import org.example.carrentapp.service.CarService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/cars")
@Tag(name = "Cars", description = "Operations related to cars")
public class CarController {

    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }

    @GetMapping
    @Operation(summary = "List all cars", description = "Retrieves all cars.")
    public ResponseEntity<List<Car>> getAllCars() {
        return ResponseEntity.ok(carService.getAllCars());
    }

    @GetMapping("/available")
    @Operation(summary = "List available cars", description = "Retrieves only cars with available=true.")
    public ResponseEntity<List<Car>> getAvailableCars() {
        return ResponseEntity.ok(carService.findAvailableCars());
    }

    @PostMapping
    @Operation(summary = "Create a new car", description = "Adds a new car to the database.")
    public ResponseEntity<Car> createCar(@RequestBody Car car) {
        Car created = carService.createCar(car);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get car by ID", description = "Retrieves a car by ID.")
    public ResponseEntity<Car> getCarById(@PathVariable Long id) {
        Car found = carService.getCarById(id);
        return found != null
                ? ResponseEntity.ok(found)
                : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update car", description = "Updates details of an existing car.")
    public ResponseEntity<Car> updateCar(
            @PathVariable Long id,
            @RequestBody Car payload
    ) {
        Car updated = carService.updateCar(id, payload);
        return updated != null
                ? ResponseEntity.ok(updated)
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete car", description = "Deletes a car by ID.")
    public ResponseEntity<Void> deleteCar(@PathVariable Long id) {
        carService.deleteCar(id);
        return ResponseEntity.ok().build();
    }
}
