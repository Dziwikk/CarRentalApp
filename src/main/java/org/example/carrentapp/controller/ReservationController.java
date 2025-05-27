// src/main/java/org/example/carrentapp/controller/ReservationController.java
package org.example.carrentapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.carrentapp.dto.ReservationDto;
import org.example.carrentapp.entity.Reservation;
import org.example.carrentapp.service.ReservationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@Tag(name = "Reservations", description = "Operations related to reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    @Operation(summary = "List all reservations", description = "Retrieves all reservations.")
    public ResponseEntity<List<Reservation>> getAllReservations() {
        return ResponseEntity.ok(reservationService.getAllReservations());
    }

    @PostMapping
    @Operation(summary = "Create a new reservation", description = "Adds a new reservation to the database.")
    public ResponseEntity<Void> createReservation(@RequestBody ReservationDto dto) {
        Long newId = reservationService.createReservation(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(newId)
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get reservation by ID", description = "Retrieves a reservation by ID.")
    public ResponseEntity<Reservation> getReservationById(@PathVariable Long id) {
        Reservation found = reservationService.getReservationById(id);
        return found != null
                ? ResponseEntity.ok(found)
                : ResponseEntity.notFound().build();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update reservation", description = "Updates dates of an existing reservation.")
    public ResponseEntity<Reservation> updateReservation(
            @PathVariable Long id,
            @RequestBody ReservationDto dto             // <-- tu było Reservation, musi być ReservationDto
    ) {
        Reservation updated = reservationService.updateReservation(id, dto);
        return updated != null
                ? ResponseEntity.ok(updated)
                : ResponseEntity.notFound().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete reservation", description = "Deletes a reservation by ID.")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        boolean deleted = reservationService.deleteReservation(id);
        return deleted
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }
}
