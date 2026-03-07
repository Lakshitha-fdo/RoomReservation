package com.oceanview.api;

import com.oceanview.client.ApiClient;
import com.oceanview.client.ClientResult;
import com.oceanview.config.DatabaseConnectionManager;
import com.oceanview.model.Bill;
import com.oceanview.model.Reservation;
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

        Reservation reservation = new Reservation(
                "R200",
                "Nimal Perera",
                "Matara",
                "0777654321",
                RoomType.DELUXE,
                LocalDate.of(2025, 11, 1),
                LocalDate.of(2025, 11, 3));

        ClientResult<Void> saveResult = apiClient.addReservation(reservation);
        assertTrue(saveResult.success());

        ClientResult<Reservation> findResult = apiClient.getReservation("R200");
        assertTrue(findResult.success());
        assertEquals("Nimal Perera", findResult.data().getGuestName());

        ClientResult<Bill> billResult = apiClient.getBill("R200");
        assertTrue(billResult.success());
        assertEquals(2, billResult.data().getNights());
        assertEquals(7000.0, billResult.data().getTotal());
    }
}
