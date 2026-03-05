package com.oceanview.dao;

import com.oceanview.config.DatabaseConnectionManager;
import com.oceanview.dao.jdbc.JdbcReservationDao;
import com.oceanview.model.Reservation;
import com.oceanview.model.RoomType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReservationDaoTest {

    @TempDir
    Path tempDir;

    private ReservationDao reservationDao;

    @BeforeEach
    void setUp() {
        DatabaseConnectionManager.resetForTests();
        String dbUrl = "jdbc:sqlite:" + tempDir.resolve("dao-test.db").toString();
        DatabaseConnectionManager connectionManager = DatabaseConnectionManager.getInstance(dbUrl);
        reservationDao = new JdbcReservationDao(connectionManager);
    }

    @Test
    void shouldInsertAndFindReservation() {
        Reservation reservation = new Reservation(
                "R100",
                "Alice Fernando",
                "Colombo",
                "0711111111",
                RoomType.SUITE,
                LocalDate.of(2025, 12, 1),
                LocalDate.of(2025, 12, 4));

        boolean inserted = reservationDao.addReservation(reservation);
        Optional<Reservation> found = reservationDao.findById("R100");

        assertTrue(inserted);
        assertTrue(found.isPresent());
        assertEquals("Alice Fernando", found.get().getGuestName());
        assertEquals(RoomType.SUITE, found.get().getRoomType());
    }
}
