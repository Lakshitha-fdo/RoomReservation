package com.oceanview.dao;

import com.oceanview.model.Reservation;

import java.util.Optional;

public interface ReservationDao {
    boolean addReservation(Reservation reservation);

    Optional<Reservation> findById(String reservationId);

    boolean existsById(String reservationId);
}
