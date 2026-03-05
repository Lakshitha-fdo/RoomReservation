package com.oceanview.service;

import com.oceanview.dao.ReservationDao;
import com.oceanview.model.Reservation;
import com.oceanview.model.ServiceResult;
import com.oceanview.util.ValidationUtil;

import java.util.List;

public class ReservationService {
    private final ReservationDao reservationDao;

    public ReservationService(ReservationDao reservationDao) {
        this.reservationDao = reservationDao;
    }

    public ServiceResult<Void> addReservation(Reservation reservation) {
        List<String> errors = ValidationUtil.validateReservation(reservation);
        if (!errors.isEmpty()) {
            return ServiceResult.fail(String.join(" ", errors));
        }

        if (reservationDao.existsById(reservation.getReservationId())) {
            return ServiceResult.fail("Reservation number already exists.");
        }

        boolean saved = reservationDao.addReservation(reservation);
        return saved ? ServiceResult.ok("Reservation saved successfully.", null)
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
}
