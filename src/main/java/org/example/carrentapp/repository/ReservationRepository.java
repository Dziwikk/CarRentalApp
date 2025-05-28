package org.example.carrentapp.repository;

import org.example.carrentapp.entity.Car;
import org.example.carrentapp.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUserId(Long userId);

    List<Reservation> findByCarAndStartDateBeforeAndEndDateAfter(Car car, LocalDate startDate, LocalDate endDate);

    List<Reservation> findByCarIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(
            Long carId, LocalDate end, LocalDate start);
}