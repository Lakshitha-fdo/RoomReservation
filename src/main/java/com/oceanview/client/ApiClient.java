package com.oceanview.client;

import com.oceanview.model.Bill;
import com.oceanview.model.Reservation;
import com.oceanview.model.RoomType;
import com.oceanview.util.SimpleJson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.Map;

public class ApiClient {
    private final String baseUrl;

    public ApiClient(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public ClientResult<Void> login(String username, String password) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("username", username);
        request.put("password", password);

        HttpResponse response = send("POST", "/api/login", SimpleJson.toJson(request));
        Map<String, String> payload = SimpleJson.parseObject(response.body());
        boolean success = Boolean.parseBoolean(payload.getOrDefault("success", "false"));
        String message = payload.getOrDefault("message", "Unexpected response.");
        return success ? ClientResult.ok(message, null) : ClientResult.fail(message);
    }

    public ClientResult<Void> addReservation(Reservation reservation) {
        Map<String, Object> request = new LinkedHashMap<>();
        request.put("reservationId", reservation.getReservationId());
        request.put("guestName", reservation.getGuestName());
        request.put("address", reservation.getAddress());
        request.put("contactNumber", reservation.getContactNumber());
        request.put("roomType", reservation.getRoomType().name());
        request.put("checkInDate", reservation.getCheckInDate().toString());
        request.put("checkOutDate", reservation.getCheckOutDate().toString());

        HttpResponse response = send("POST", "/api/reservations", SimpleJson.toJson(request));
        Map<String, String> payload = SimpleJson.parseObject(response.body());
        boolean success = Boolean.parseBoolean(payload.getOrDefault("success", "false"));
        String message = payload.getOrDefault("message", "Unexpected response.");
        return success ? ClientResult.ok(message, null) : ClientResult.fail(message);
    }

    public ClientResult<Reservation> getReservation(String reservationId) {
        String encodedId = URLEncoder.encode(reservationId, StandardCharsets.UTF_8);
        HttpResponse response = send("GET", "/api/reservations/" + encodedId, null);
        Map<String, String> payload = SimpleJson.parseObject(response.body());
        boolean success = Boolean.parseBoolean(payload.getOrDefault("success", "false"));
        if (!success) {
            return ClientResult.fail(payload.getOrDefault("message", "Reservation not found."));
        }

        Reservation reservation = new Reservation(
                payload.get("reservationId"),
                payload.get("guestName"),
                payload.get("address"),
                payload.get("contactNumber"),
                RoomType.from(payload.get("roomType")),
                LocalDate.parse(payload.get("checkInDate")),
                LocalDate.parse(payload.get("checkOutDate")));

        return ClientResult.ok(payload.getOrDefault("message", "Reservation found."), reservation);
    }

    public ClientResult<Bill> getBill(String reservationId) {
        String encodedId = URLEncoder.encode(reservationId, StandardCharsets.UTF_8);
        HttpResponse response = send("GET", "/api/bill/" + encodedId, null);
        Map<String, String> payload = SimpleJson.parseObject(response.body());
        boolean success = Boolean.parseBoolean(payload.getOrDefault("success", "false"));
        if (!success) {
            return ClientResult.fail(payload.getOrDefault("message", "Bill cannot be generated."));
        }

        Bill bill = new Bill(
                payload.get("reservationId"),
                Long.parseLong(payload.get("nights")),
                Double.parseDouble(payload.get("nightlyRate")),
                Double.parseDouble(payload.get("total")));

        return ClientResult.ok(payload.getOrDefault("message", "Bill generated."), bill);
    }

    private HttpResponse send(String method, String path, String jsonBody) {
        HttpURLConnection connection = null;
        try {
            URI uri = URI.create(baseUrl + path);
            connection = (HttpURLConnection) uri.toURL().openConnection();
            connection.setRequestMethod(method);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("Accept", "application/json");

            if (jsonBody != null) {
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                byte[] body = jsonBody.getBytes(StandardCharsets.UTF_8);
                try (OutputStream os = connection.getOutputStream()) {
                    os.write(body);
                }
            }

            int statusCode = connection.getResponseCode();
            String responseBody;
            try (InputStream inputStream = statusCode >= 400 ? connection.getErrorStream() : connection.getInputStream()) {
                responseBody = inputStream == null ? "{}" : new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }

            return new HttpResponse(statusCode, responseBody);
        } catch (IOException e) {
            return new HttpResponse(500, "{\"success\":false,\"message\":\"Service unreachable. Start API server first.\"}");
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private record HttpResponse(int statusCode, String body) {
    }
}
