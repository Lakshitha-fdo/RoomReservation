package com.oceanview;

import com.oceanview.api.ApiServer;
import com.oceanview.ui.view.ClientMain;

public class ApplicationMain {
    public static void main(String[] args) throws Exception {
        int port = Integer.parseInt(System.getProperty("server.port", "8080"));
        String dbUrl = System.getProperty("db.url", "jdbc:sqlite:data/ocean_view.db");

        ApiServer apiServer = ApiServer.create(port, dbUrl);
        apiServer.start();

        Runtime.getRuntime().addShutdownHook(new Thread(apiServer::stop));
        ClientMain.main(args);
    }
}
