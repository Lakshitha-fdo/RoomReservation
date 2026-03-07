package com.oceanview.api;

import com.oceanview.client.ApiClient;
import com.oceanview.client.ClientResult;
import com.oceanview.config.DatabaseConnectionManager;
import com.oceanview.model.Bill;
import com.oceanview.model.Reservation;
import com.oceanview.model.ReservationPage;
import com.oceanview.model.RoomType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiServerIntegrationTest {

    @TempDir
    Path tempDir;

    private ApiServer apiServer;
    private ApiClient apiClient;

    @BeforeEach
    void setUp() throws Exception {
        DatabaseConnectionManager.resetForTests();
        String dbUrl = "jdbc:sqlite:" + tempDir.resolve("api-test.db").toString();
        apiServer = ApiServer.create(0, dbUrl);
        apiServer.start();
        apiClient = new ApiClient("http://localhost:" + apiServer.getPort());
    }

    @AfterEach
    void tearDown() {
        if (apiServer != null) {
            apiServer.stop();
        }
        DatabaseConnectionManager.resetForTests();
    }

    @Test
    void shouldSupportLoginReservationAndBillingFlow() {
        ClientResult<Void> loginResult = apiClient.login("admin", "1234");
        assertTrue(loginResult.success());

        ClientResult<String> nextIdResult = apiClient.getNextReservationId();
        assertTrue(nextIdResult.success());
        assertEquals("1", nextIdResult.data());

        Reservation reservation = new Reservation(
                nextIdResult.data(),
                "Nimal Perera",
                "Matara",
                "0777654321",
                RoomType.DELUXE,
                LocalDate.of(2025, 11, 1),
                LocalDate.of(2025, 11, 3));

        ClientResult<Void> saveResult = apiClient.addReservation(reservation);
        assertTrue(saveResult.success());

        ClientResult<Reservation> findResult = apiClient.getReservation("1");
        assertTrue(findResult.success());
        assertEquals("Nimal Perera", findResult.data().getGuestName());

        ClientResult<ReservationPage> reservationPageResult = apiClient.getReservations("Nimal", 1, 8);
        assertTrue(reservationPageResult.success());
        assertEquals(1, reservationPageResult.data().getReservations().size());
        assertEquals(1, reservationPageResult.data().getTotalItems());

        Reservation updatedReservation = new Reservation(
                "1",
                "Nimal Perera",
                "Matara",
                "0777654321",
                RoomType.SUITE,
                LocalDate.of(2025, 11, 2),
                LocalDate.of(2025, 11, 5));

        ClientResult<Void> updateResult = apiClient.updateReservation(updatedReservation);
        assertTrue(updateResult.success());

        ClientResult<Reservation> updatedFindResult = apiClient.getReservation("1");
        assertTrue(updatedFindResult.success());
        assertEquals(RoomType.SUITE, updatedFindResult.data().getRoomType());
        assertEquals(LocalDate.of(2025, 11, 5), updatedFindResult.data().getCheckOutDate());

        ClientResult<Bill> billResult = apiClient.getBill("1");
        assertTrue(billResult.success());
        assertEquals(3, billResult.data().getNights());
        assertEquals(4500.0, billResult.data().getNightlyRate());
        assertEquals(13500.0, billResult.data().getTotal());
    }
}
