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
import java.util.List;
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
                "1",
                "Alice Fernando",
                "Colombo",
                "0711111111",
                RoomType.SUITE,
                LocalDate.of(2025, 12, 1),
                LocalDate.of(2025, 12, 4));

        boolean inserted = reservationDao.addReservation(reservation);
        Optional<Reservation> found = reservationDao.findById("1");

        assertTrue(inserted);
        assertTrue(found.isPresent());
        assertEquals("Alice Fernando", found.get().getGuestName());
        assertEquals(RoomType.SUITE, found.get().getRoomType());
    }

    @Test
    void shouldGenerateNextIdSupportPagingAndUpdateReservation() {
        Reservation firstReservation = new Reservation(
                "1",
                "Alice Fernando",
                "Colombo",
                "0711111111",
                RoomType.STANDARD,
                LocalDate.of(2025, 12, 1),
                LocalDate.of(2025, 12, 3));
        Reservation secondReservation = new Reservation(
                "2",
                "Kamal Silva",
                "Galle",
                "0722222222",
                RoomType.DELUXE,
                LocalDate.of(2025, 12, 5),
                LocalDate.of(2025, 12, 8));

        assertTrue(reservationDao.addReservation(firstReservation));
        assertTrue(reservationDao.addReservation(secondReservation));
        assertEquals("3", reservationDao.getNextReservationId());

        List<Reservation> searchResults = reservationDao.findAll("Kamal", 1, 8);
        assertEquals(1, searchResults.size());
        assertEquals("2", searchResults.get(0).getReservationId());
        assertEquals(2, reservationDao.countAll(""));

        Reservation updatedReservation = new Reservation(
                "2",
                "Kamal Silva",
                "Galle",
                "0722222222",
                RoomType.SUITE,
                LocalDate.of(2025, 12, 6),
                LocalDate.of(2025, 12, 10));

        assertTrue(reservationDao.updateReservation(updatedReservation));

        Optional<Reservation> found = reservationDao.findById("2");
        assertTrue(found.isPresent());
        assertEquals(RoomType.SUITE, found.get().getRoomType());
        assertEquals(LocalDate.of(2025, 12, 10), found.get().getCheckOutDate());
    }
}
