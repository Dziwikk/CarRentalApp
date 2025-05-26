package org.example.carrentapp.service;

import org.example.carrentapp.entity.Reservation;
import org.example.carrentapp.repository.ReservationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReservationService {
    private final ReservationRepository repo;

    public ReservationService(ReservationRepository repo) {
        this.repo = repo;
    }

    public List<Reservation> getAllReservations() {
        return repo.findAll();
    }

    public Reservation createReservation(Reservation reservation) {
        return repo.save(reservation);
    }

    public Reservation getReservationById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public Reservation updateReservation(Long id, Reservation payload) {
        return repo.findById(id)
                .map(existing -> {
                    existing.setStartDate(payload.getStartDate());
                    existing.setEndDate(payload.getEndDate());
                    return repo.save(existing);
                })
                .orElse(null);
    }

    public boolean deleteReservation(Long id) {
        if (repo.existsById(id)) {
            repo.deleteById(id);
            return true;
        }
        return false;
    }
}