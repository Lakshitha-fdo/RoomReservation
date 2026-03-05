package com.oceanview.service;

import com.oceanview.model.Bill;
import com.oceanview.model.Reservation;
import com.oceanview.model.ServiceResult;
import com.oceanview.util.BillCalculator;
import com.oceanview.util.DateUtil;
import com.oceanview.util.RoomRateFactory;

public class BillingService {
    private final ReservationService reservationService;

    public BillingService(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    public ServiceResult<Bill> generateBill(String reservationId) {
        ServiceResult<Reservation> reservationResult = reservationService.findById(reservationId);
        if (!reservationResult.success()) {
            return ServiceResult.fail(reservationResult.message());
        }

        Reservation reservation = reservationResult.data();
        long nights = DateUtil.calculateNights(reservation.getCheckInDate(), reservation.getCheckOutDate());
        double rate = RoomRateFactory.getNightlyRate(reservation.getRoomType());
        double total = BillCalculator.calculateTotal(nights, rate);

        Bill bill = new Bill(reservation.getReservationId(), nights, rate, total);
        return ServiceResult.ok("Bill generated successfully.", bill);
    }
}
