package com.oceanview.dao.jdbc;

import com.oceanview.config.DatabaseConnectionManager;
import com.oceanview.dao.ReservationDao;
import com.oceanview.model.Reservation;
import com.oceanview.model.RoomType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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

                return Optional.of(mapReservation(rs));
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

    @Override
    public String getNextReservationId() {
        String sql = """
                SELECT COALESCE(MAX(CAST(reservation_id AS INTEGER)), 0) + 1
                FROM reservations
                WHERE TRIM(reservation_id) <> ''
                  AND reservation_id NOT GLOB '*[^0-9]*'
                """;
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql);
             ResultSet resultSet = preparedStatement.executeQuery()) {
            return resultSet.next() ? String.valueOf(resultSet.getInt(1)) : "1";
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to generate next reservation number", e);
        }
    }

    @Override
    public List<Reservation> findAll(String searchTerm, int page, int pageSize) {
        int safePage = Math.max(1, page);
        int safePageSize = Math.max(1, pageSize);
        int offset = (safePage - 1) * safePageSize;

        String sql = """
                SELECT reservation_id, guest_name, address, contact_number, room_type, checkin_date, checkout_date
                FROM reservations
                WHERE (? = '' OR reservation_id LIKE ? OR guest_name LIKE ? OR address LIKE ? OR contact_number LIKE ? OR room_type LIKE ?)
                ORDER BY
                    CASE
                        WHEN TRIM(reservation_id) <> '' AND reservation_id NOT GLOB '*[^0-9]*'
                            THEN CAST(reservation_id AS INTEGER)
                    END ASC,
                    reservation_id ASC
                LIMIT ? OFFSET ?
                """;

        try (Connection connection = connectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            String normalizedSearch = normalizeSearch(searchTerm);
            preparedStatement.setString(1, normalizedSearch.isEmpty() ? "" : normalizedSearch);
            preparedStatement.setString(2, likeTerm(normalizedSearch));
            preparedStatement.setString(3, likeTerm(normalizedSearch));
            preparedStatement.setString(4, likeTerm(normalizedSearch));
            preparedStatement.setString(5, likeTerm(normalizedSearch));
            preparedStatement.setString(6, likeTerm(normalizedSearch));
            preparedStatement.setInt(7, safePageSize);
            preparedStatement.setInt(8, offset);

            List<Reservation> reservations = new ArrayList<>();
            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    reservations.add(mapReservation(resultSet));
                }
            }
            return reservations;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to fetch reservations", e);
        }
    }

    @Override
    public int countAll(String searchTerm) {
        String sql = """
                SELECT COUNT(1)
                FROM reservations
                WHERE (? = '' OR reservation_id LIKE ? OR guest_name LIKE ? OR address LIKE ? OR contact_number LIKE ? OR room_type LIKE ?)
                """;

        try (Connection connection = connectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            String normalizedSearch = normalizeSearch(searchTerm);
            preparedStatement.setString(1, normalizedSearch.isEmpty() ? "" : normalizedSearch);
            preparedStatement.setString(2, likeTerm(normalizedSearch));
            preparedStatement.setString(3, likeTerm(normalizedSearch));
            preparedStatement.setString(4, likeTerm(normalizedSearch));
            preparedStatement.setString(5, likeTerm(normalizedSearch));
            preparedStatement.setString(6, likeTerm(normalizedSearch));

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                return resultSet.next() ? resultSet.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to count reservations", e);
        }
    }

    @Override
    public boolean updateReservation(Reservation reservation) {
        String sql = """
                UPDATE reservations
                SET guest_name = ?, address = ?, contact_number = ?, room_type = ?, checkin_date = ?, checkout_date = ?
                WHERE reservation_id = ?
                """;
        try (Connection connection = connectionManager.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, reservation.getGuestName());
            preparedStatement.setString(2, reservation.getAddress());
            preparedStatement.setString(3, reservation.getContactNumber());
            preparedStatement.setString(4, reservation.getRoomType().name());
            preparedStatement.setString(5, reservation.getCheckInDate().toString());
            preparedStatement.setString(6, reservation.getCheckOutDate().toString());
            preparedStatement.setString(7, reservation.getReservationId());
            return preparedStatement.executeUpdate() == 1;
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to update reservation", e);
        }
    }

    private Reservation mapReservation(ResultSet resultSet) throws SQLException {
        return new Reservation(
                resultSet.getString("reservation_id"),
                resultSet.getString("guest_name"),
                resultSet.getString("address"),
                resultSet.getString("contact_number"),
                RoomType.from(resultSet.getString("room_type")),
                java.time.LocalDate.parse(resultSet.getString("checkin_date")),
                java.time.LocalDate.parse(resultSet.getString("checkout_date")));
    }

    private String normalizeSearch(String searchTerm) {
        return searchTerm == null ? "" : searchTerm.trim();
    }

    private String likeTerm(String searchTerm) {
        return "%" + searchTerm + "%";
    }
}
