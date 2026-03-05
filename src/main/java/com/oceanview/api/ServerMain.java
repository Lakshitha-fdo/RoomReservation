package com.oceanview.api;

public class ServerMain {
    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getProperty("server.port", "8080"));
        String dbUrl = System.getProperty("db.url", "jdbc:sqlite:data/ocean_view.db");

        ApiServer apiServer = ApiServer.create(port, dbUrl);
        apiServer.start();

        System.out.println("Ocean View API server started on port " + apiServer.getPort());
        System.out.println("Press CTRL+C to stop.");
    }
}
