package org.example.carrentapp.service;

import org.example.carrentapp.entity.Car;
import org.example.carrentapp.repository.CarRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CarService {
    private final CarRepository repo;

    public CarService(CarRepository repo) {
        this.repo = repo;
    }

    public List<Car> getAllCars() {
        return repo.findAll();
    }

    public Car createCar(Car car) {
        return repo.save(car);
    }

    public Car getCarById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public Car updateCar(Long id, Car payload) {
        return repo.findById(id)
                .map(existing -> {
                    existing.setMake(payload.getMake());
                    existing.setModel(payload.getModel());
                    existing.setYear(payload.getYear());
                    existing.setAvailable(payload.isAvailable());
                    return repo.save(existing);
                })
                .orElse(null);
    }

    public boolean deleteCar(Long id) {
        if (repo.existsById(id)) {
            repo.deleteById(id);
            return true;
        }
        return false;
    }

    // <<< nowa metoda >>>
    public List<Car> findAvailableCars() {
        return repo.findByAvailableTrue();
    }
}
