package com.oceanview.dao.jdbc;

import com.oceanview.config.DatabaseConnectionManager;
import com.oceanview.dao.ReservationDao;
import com.oceanview.model.Reservation;
import com.oceanview.model.RoomType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class JdbcReservationDao implements ReservationDao {
    private final DatabaseConnectionManager connectionManager;

    public JdbcReservationDao(DatabaseConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public boolean addReservation(Reservation reservation) {
        String sql = """
                INSERT INTO reservations
                (reservation_id, guest_name, address, contact_number, room_type, checkin_date, checkout_date)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, reservation.getReservationId());
            preparedStatement.setString(2, reservation.getGuestName());
            preparedStatement.setString(3, reservation.getAddress());
            preparedStatement.setString(4, reservation.getContactNumber());
            preparedStatement.setString(5, reservation.getRoomType().name());
            preparedStatement.setString(6, reservation.getCheckInDate().toString());
            preparedStatement.setString(7, reservation.getCheckOutDate().toString());

            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public Optional<Reservation> findById(String reservationId) {
        String sql = """
                SELECT reservation_id, guest_name, address, contact_number, room_type, checkin_date, checkout_date
                FROM reservations
                WHERE reservation_id = ?
                """;
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, reservationId);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }

                Reservation reservation = new Reservation(
                        rs.getString("reservation_id"),
                        rs.getString("guest_name"),
                        rs.getString("address"),
                        rs.getString("contact_number"),
                        RoomType.from(rs.getString("room_type")),
                        java.time.LocalDate.parse(rs.getString("checkin_date")),
                        java.time.LocalDate.parse(rs.getString("checkout_date")));
                return Optional.of(reservation);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to fetch reservation", e);
        }
    }

    @Override
    public boolean existsById(String reservationId) {
        String sql = "SELECT COUNT(1) FROM reservations WHERE reservation_id = ?";
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, reservationId);
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() && resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to check reservation existence", e);
        }
    }
}
