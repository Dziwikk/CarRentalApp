// src/main/java/org/example/carrentapp/service/ReservationService.java
package org.example.carrentapp.service;

import org.example.carrentapp.dto.ReservationDto;
import org.example.carrentapp.entity.Car;
import org.example.carrentapp.entity.Reservation;
import org.example.carrentapp.entity.User;
import org.example.carrentapp.repository.CarRepository;
import org.example.carrentapp.repository.ReservationRepository;
import org.example.carrentapp.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepo;
    private final CarRepository carRepo;
    private final UserRepository userRepo;

    public ReservationService(ReservationRepository reservationRepo,
                              CarRepository carRepo,
                              UserRepository userRepo) {
        this.reservationRepo = reservationRepo;
        this.carRepo = carRepo;
        this.userRepo = userRepo;
    }

    public List<Reservation> getAllReservations() {
        return reservationRepo.findAll();
    }

    @Transactional
    public Long createReservation(ReservationDto dto) {
        // 1) znajdź auto, zbłądź jeżeli brak
        Car car = carRepo.findById(dto.getCarId())
                .orElseThrow(() -> new EntityNotFoundException("Car not found: " + dto.getCarId()));
        if (!car.isAvailable()) {
            throw new IllegalStateException("Car is already reserved");
        }

        // 2) znajdź użytkownika
        User user = userRepo.findById(dto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + dto.getUserId()));

        // 3) oznacz auto jako zajęte
        car.setAvailable(false);
        carRepo.save(car);

        // 4) stwórz rezerwację
        Reservation res = new Reservation();
        res.setCar(car);
        res.setUser(user);
        res.setStartDate(dto.getStartDate());
        res.setEndDate(dto.getEndDate());
        Reservation saved = reservationRepo.save(res);

        return saved.getId();
    }

    public Reservation getReservationById(Long id) {
        return reservationRepo.findById(id).orElse(null);
    }

    @Transactional
    public Reservation updateReservation(Long id, ReservationDto dto) {
        return reservationRepo.findById(id)
                .map(existing -> {
                    existing.setStartDate(dto.getStartDate());
                    existing.setEndDate(dto.getEndDate());
                    return reservationRepo.save(existing);
                })
                .orElse(null);
    }

    @Transactional
    public boolean deleteReservation(Long id) {
        if (reservationRepo.existsById(id)) {
            reservationRepo.deleteById(id);
            return true;
        }
        return false;
    }
}
