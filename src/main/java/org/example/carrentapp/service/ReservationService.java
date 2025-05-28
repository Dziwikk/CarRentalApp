package org.example.carrentapp.service;

import org.example.carrentapp.available.IfAvailable;
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
        // 1) Znajdź samochód
        Car car = carRepo.findById(dto.getCarId())
                .orElseThrow(() -> new EntityNotFoundException("Car not found: " + dto.getCarId()));

        // 2) Sprawdź, czy samochód jest dostępny
        if (!car.getAvailable()) {
            throw new IllegalStateException("Car is already reserved");
        }

        // 3) Znajdź użytkownika
        User user = userRepo.findById(dto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + dto.getUserId()));

        // 4) Zarezerwuj auto
        car.setAvailable(false);
        carRepo.save(car); // Zapisujemy, że auto jest teraz niedostępne

        // 5) Stwórz rezerwację
        Reservation res = new Reservation();
        res.setCar(car);
        res.setUser(user);
        res.setStartDate(dto.getStartDate());
        res.setEndDate(dto.getEndDate());
        Reservation saved = reservationRepo.save(res);

        return saved.getId(); // Zwróć ID zapisanej rezerwacji
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
