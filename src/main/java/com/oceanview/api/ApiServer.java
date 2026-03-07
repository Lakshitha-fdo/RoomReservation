package com.oceanview.api;

import com.oceanview.config.DatabaseConnectionManager;
import com.oceanview.dao.jdbc.JdbcReservationDao;
import com.oceanview.dao.jdbc.JdbcUserDao;
import com.oceanview.model.Bill;
import com.oceanview.model.Reservation;
import com.oceanview.model.ReservationPage;
import com.oceanview.model.RoomType;
import com.oceanview.model.ServiceResult;
import com.oceanview.service.AuthService;
import com.oceanview.service.BillingService;
import com.oceanview.service.ReservationService;
import com.oceanview.util.SimpleJson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApiServer {
    private final HttpServer server;
    private final ExecutorService executorService;

    private ApiServer(HttpServer server, ExecutorService executorService) {
        this.server = server;
        this.executorService = executorService;
    }

    public static ApiServer create(int port, String dbUrl) throws IOException {
        DatabaseConnectionManager connectionManager = DatabaseConnectionManager.getInstance(dbUrl);
        AuthService authService = new AuthService(new JdbcUserDao(connectionManager));
        ReservationService reservationService = new ReservationService(new JdbcReservationDao(connectionManager));
        BillingService billingService = new BillingService(reservationService);

        HttpServer httpServer = HttpServer.create(new InetSocketAddress(port), 0);
        ExecutorService executor = Executors.newFixedThreadPool(8);
        httpServer.setExecutor(executor);

        httpServer.createContext("/api/login", new LoginHandler(authService));
        httpServer.createContext("/api/reservations", new ReservationHandler(reservationService));
        httpServer.createContext("/api/bill", new BillHandler(billingService));

        return new ApiServer(httpServer, executor);
    }

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop(0);
        executorService.shutdownNow();
    }

    public int getPort() {
        return server.getAddress().getPort();
    }

    private static class LoginHandler implements HttpHandler {
        private final AuthService authService;

        private LoginHandler(AuthService authService) {
            this.authService = authService;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJson(exchange, 405, Map.of("success", false, "message", "Method not allowed."));
                return;
            }

            try {
                Map<String, String> request = SimpleJson.parseObject(readRequestBody(exchange));
                ServiceResult<Void> result = authService.login(request.get("username"), request.get("password"));
                int status = result.success() ? 200 : 401;
                sendJson(exchange, status, Map.of("success", result.success(), "message", result.message()));
            } catch (Exception e) {
                sendJson(exchange, 400, Map.of("success", false, "message", "Invalid request payload."));
            }
        }
    }

    private static class ReservationHandler implements HttpHandler {
        private final ReservationService reservationService;

        private ReservationHandler(ReservationService reservationService) {
            this.reservationService = reservationService;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if ("POST".equalsIgnoreCase(method) && "/api/reservations".equals(path)) {
                handleCreate(exchange);
                return;
            }

            if ("GET".equalsIgnoreCase(method) && "/api/reservations".equals(path)) {
                handleList(exchange);
                return;
            }

            if ("GET".equalsIgnoreCase(method) && "/api/reservations/next-id".equals(path)) {
                handleNextId(exchange);
                return;
            }

            if ("PUT".equalsIgnoreCase(method) && path.startsWith("/api/reservations/")) {
                String reservationId = path.substring("/api/reservations/".length()).trim();
                handleUpdate(exchange, reservationId);
                return;
            }

            if ("GET".equalsIgnoreCase(method) && path.startsWith("/api/reservations/")) {
                String reservationId = path.substring("/api/reservations/".length()).trim();
                handleFind(exchange, reservationId);
                return;
            }

            sendJson(exchange, 404, Map.of("success", false, "message", "Endpoint not found."));
        }

        private void handleCreate(HttpExchange exchange) throws IOException {
            try {
                Map<String, String> request = SimpleJson.parseObject(readRequestBody(exchange));
                Reservation reservation = new Reservation(
                        request.get("reservationId"),
                        request.get("guestName"),
                        request.get("address"),
                        request.get("contactNumber"),
                        RoomType.from(request.get("roomType")),
                        LocalDate.parse(request.get("checkInDate")),
                        LocalDate.parse(request.get("checkOutDate")));

                ServiceResult<Void> result = reservationService.addReservation(reservation);
                int status = result.success() ? 201 : 400;
                sendJson(exchange, status, Map.of("success", result.success(), "message", result.message()));
            } catch (IllegalArgumentException e) {
                sendJson(exchange, 400, Map.of("success", false, "message", e.getMessage()));
            } catch (Exception e) {
                sendJson(exchange, 400, Map.of("success", false, "message", "Invalid request payload."));
            }
        }

        private void handleFind(HttpExchange exchange, String reservationId) throws IOException {
            ServiceResult<Reservation> result = reservationService.findById(reservationId);
            if (!result.success()) {
                sendJson(exchange, 404, Map.of("success", false, "message", result.message()));
                return;
            }

            Reservation reservation = result.data();
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("message", result.message());
            response.put("reservationId", reservation.getReservationId());
            response.put("guestName", reservation.getGuestName());
            response.put("address", reservation.getAddress());
            response.put("contactNumber", reservation.getContactNumber());
            response.put("roomType", reservation.getRoomType().name());
            response.put("checkInDate", reservation.getCheckInDate().toString());
            response.put("checkOutDate", reservation.getCheckOutDate().toString());
            sendJson(exchange, 200, response);
        }

        private void handleNextId(HttpExchange exchange) throws IOException {
            ServiceResult<String> result = reservationService.getNextReservationId();
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", result.success());
            response.put("message", result.message());
            response.put("reservationId", result.data());
            sendJson(exchange, 200, response);
        }

        private void handleList(HttpExchange exchange) throws IOException {
            Map<String, String> query = parseQuery(exchange.getRequestURI().getRawQuery());
            int page = parsePositiveInt(query.get("page"), 1);
            int pageSize = parsePositiveInt(query.get("pageSize"), 8);
            String search = query.getOrDefault("search", "");

            ServiceResult<ReservationPage> result = reservationService.findReservations(search, page, pageSize);
            ReservationPage reservationPage = result.data();
            List<Map<String, Object>> reservations = new ArrayList<>();
            for (Reservation reservation : reservationPage.getReservations()) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("reservationId", reservation.getReservationId());
                item.put("guestName", reservation.getGuestName());
                item.put("address", reservation.getAddress());
                item.put("contactNumber", reservation.getContactNumber());
                item.put("roomType", reservation.getRoomType().name());
                item.put("checkInDate", reservation.getCheckInDate().toString());
                item.put("checkOutDate", reservation.getCheckOutDate().toString());
                reservations.add(item);
            }

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", result.success());
            response.put("message", result.message());
            response.put("page", reservationPage.getCurrentPage());
            response.put("pageSize", reservationPage.getPageSize());
            response.put("totalItems", reservationPage.getTotalItems());
            response.put("totalPages", reservationPage.getTotalPages());
            response.put("reservations", reservations);
            sendJson(exchange, 200, response);
        }

        private void handleUpdate(HttpExchange exchange, String reservationId) throws IOException {
            try {
                Map<String, String> request = SimpleJson.parseObject(readRequestBody(exchange));
                Reservation reservation = new Reservation(
                        reservationId,
                        request.get("guestName"),
                        request.get("address"),
                        request.get("contactNumber"),
                        RoomType.from(request.get("roomType")),
                        LocalDate.parse(request.get("checkInDate")),
                        LocalDate.parse(request.get("checkOutDate")));

                ServiceResult<Void> result = reservationService.updateReservation(reservation);
                int status = result.success() ? 200 : 400;
                if ("Reservation not found.".equals(result.message())) {
                    status = 404;
                }
                sendJson(exchange, status, Map.of("success", result.success(), "message", result.message()));
            } catch (IllegalArgumentException e) {
                sendJson(exchange, 400, Map.of("success", false, "message", e.getMessage()));
            } catch (Exception e) {
                sendJson(exchange, 400, Map.of("success", false, "message", "Invalid request payload."));
            }
        }
    }

    private static class BillHandler implements HttpHandler {
        private final BillingService billingService;

        private BillHandler(BillingService billingService) {
            this.billingService = billingService;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            String path = exchange.getRequestURI().getPath();

            if (!"GET".equalsIgnoreCase(method)) {
                sendJson(exchange, 405, Map.of("success", false, "message", "Method not allowed."));
                return;
            }

            if (!path.startsWith("/api/bill/")) {
                sendJson(exchange, 404, Map.of("success", false, "message", "Endpoint not found."));
                return;
            }

            String reservationId = path.substring("/api/bill/".length()).trim();
            ServiceResult<Bill> result = billingService.generateBill(reservationId);
            if (!result.success()) {
                sendJson(exchange, 404, Map.of("success", false, "message", result.message()));
                return;
            }

            Bill bill = result.data();
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("message", result.message());
            response.put("reservationId", bill.getReservationId());
            response.put("nights", bill.getNights());
            response.put("nightlyRate", bill.getNightlyRate());
            response.put("total", bill.getTotal());
            sendJson(exchange, 200, response);
        }
    }

    private static String readRequestBody(HttpExchange exchange) throws IOException {
        try (InputStream inputStream = exchange.getRequestBody()) {
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }

    private static void sendJson(HttpExchange exchange, int statusCode, Map<String, ?> body) throws IOException {
        String json = SimpleJson.toJson(body);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(bytes);
        }
    }

    private static Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> query = new LinkedHashMap<>();
        if (rawQuery == null || rawQuery.isBlank()) {
            return query;
        }

        String[] pairs = rawQuery.split("&");
        for (String pair : pairs) {
            if (pair.isBlank()) {
                continue;
            }
            String[] parts = pair.split("=", 2);
            String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
            String value = parts.length > 1 ? URLDecoder.decode(parts[1], StandardCharsets.UTF_8) : "";
            query.put(key, value);
        }
        return query;
    }

    private static int parsePositiveInt(String rawValue, int defaultValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return defaultValue;
        }
        try {
            int value = Integer.parseInt(rawValue.trim());
            return value > 0 ? value : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
