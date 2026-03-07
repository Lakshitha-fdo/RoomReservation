package com.oceanview.dao;

import com.oceanview.model.Reservation;

import java.util.List;
import java.util.Optional;

public interface ReservationDao {
    boolean addReservation(Reservation reservation);

    Optional<Reservation> findById(String reservationId);

    boolean existsById(String reservationId);

    String getNextReservationId();

    List<Reservation> findAll(String searchTerm, int page, int pageSize);

    int countAll(String searchTerm);

    boolean updateReservation(Reservation reservation);
}
