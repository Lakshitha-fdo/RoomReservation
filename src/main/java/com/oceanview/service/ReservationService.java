package com.oceanview.service;

import com.oceanview.dao.ReservationDao;
import com.oceanview.model.Reservation;
import com.oceanview.model.ReservationPage;
import com.oceanview.model.ServiceResult;
import com.oceanview.util.ValidationUtil;

import java.util.List;

public class ReservationService {
    private final ReservationDao reservationDao;

    public ReservationService(ReservationDao reservationDao) {
        this.reservationDao = reservationDao;
    }

    public ServiceResult<Void> addReservation(Reservation reservation) {
        if (reservation != null && ValidationUtil.isBlank(reservation.getReservationId())) {
            reservation.setReservationId(reservationDao.getNextReservationId());
        }

        List<String> errors = ValidationUtil.validateReservation(reservation);
        if (!errors.isEmpty()) {
            return ServiceResult.fail(String.join(" ", errors));
        }

        reservation.setReservationId(reservation.getReservationId().trim());
        if (reservationDao.existsById(reservation.getReservationId())) {
            return ServiceResult.fail("Reservation number already exists.");
        }

        boolean saved = reservationDao.addReservation(reservation);
        return saved ? ServiceResult.ok("Reservation saved successfully. Reservation number: " + reservation.getReservationId(), null)
                : ServiceResult.fail("Could not save reservation.");
    }

    public ServiceResult<Reservation> findById(String reservationId) {
        if (ValidationUtil.isBlank(reservationId)) {
            return ServiceResult.fail("Reservation number is required.");
        }

        return reservationDao.findById(reservationId.trim())
                .map(reservation -> ServiceResult.ok("Reservation found.", reservation))
                .orElseGet(() -> ServiceResult.fail("Reservation not found."));
    }

    public ServiceResult<String> getNextReservationId() {
        return ServiceResult.ok("Next reservation number generated.", reservationDao.getNextReservationId());
    }

    public ServiceResult<ReservationPage> findReservations(String searchTerm, int page, int pageSize) {
        int safePage = Math.max(1, page);
        int safePageSize = Math.max(1, pageSize);
        List<Reservation> reservations = reservationDao.findAll(searchTerm, safePage, safePageSize);
        int totalItems = reservationDao.countAll(searchTerm);
        ReservationPage reservationPage = new ReservationPage(reservations, safePage, safePageSize, totalItems);
        return ServiceResult.ok("Reservations loaded.", reservationPage);
    }

    public ServiceResult<Void> updateReservation(Reservation reservation) {
        List<String> errors = ValidationUtil.validateReservation(reservation);
        if (!errors.isEmpty()) {
            return ServiceResult.fail(String.join(" ", errors));
        }

        reservation.setReservationId(reservation.getReservationId().trim());
        if (!reservationDao.existsById(reservation.getReservationId())) {
            return ServiceResult.fail("Reservation not found.");
        }

        boolean updated = reservationDao.updateReservation(reservation);
        return updated ? ServiceResult.ok("Reservation updated successfully.", null)
                : ServiceResult.fail("Could not update reservation.");
    }
}
