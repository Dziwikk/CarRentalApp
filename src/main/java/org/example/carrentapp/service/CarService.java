package org.example.carrentapp.service;

import org.example.carrentapp.entity.Car;
import org.example.carrentapp.available.IfAvailable; // Importujemy interfejs IfAvailable
import org.example.carrentapp.repository.CarRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CarService {
    private final CarRepository repo;

    public CarService(CarRepository repo) {
        this.repo = repo;
    }

    // Dodajemy metodę, która zwróci wszystkie samochody
    public List<Car> getAllCars() {
        return repo.findAll(); // Zwracamy wszystkie samochody z repozytorium
    }

    // Metoda zwracająca dostępne samochody
    public List<Car> getAvailableCars() {
        List<Car> allCars = getAllCars(); // Korzystamy z getAllCars()
        // Filtrowanie samochodów na podstawie dostępności
        return allCars.stream()
                .filter(car -> car instanceof IfAvailable && ((IfAvailable) car).getAvailable()) // Polimorfizm: sprawdzamy dostępność przez interfejs
                .collect(Collectors.toList());
    }

    // Tworzenie nowego samochodu
    public Car createCar(Car car) {
        return repo.save(car);
    }

    // Pobieranie samochodu po ID
    public Car getCarById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public Car updateCar(Long id, Car payload) {
        return repo.findById(id)
                .map(existing -> {
                    existing.setMake(payload.getMake());
                    existing.setModel(payload.getModel());
                    existing.setYear(payload.getYear());
                    existing.setAvailable(payload.getAvailable()); // Zmieniamy na getAvailable()
                    return repo.save(existing);
                })
                .orElse(null);
    }

    // Usuwanie samochodu po ID
    public boolean deleteCar(Long id) {
        if (repo.existsById(id)) {
            repo.deleteById(id);
            return true;
        }
        return false;
    }

    public List<Car> findAvailableCars() {
        return repo.findAll().stream()
                .filter(car -> car != null && ((IfAvailable) car).getAvailable())
                .collect(Collectors.toList());
    }

}
