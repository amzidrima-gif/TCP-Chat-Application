package org.example;

import javafx.application.Application;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        try {
            // Read server settings from command-line arguments.
            // Defaults are used when no arguments are supplied.
            String serverHost = args.length >= 1
                    ? args[0]
                    : "localhost";

            int serverPort = args.length >= 2
                    ? Integer.parseInt(args[1])
                    : 3000;

            if (serverHost.isBlank()) {
                throw new IllegalArgumentException(
                        "Server IP address or hostname cannot be empty."
                );
            }

            if (serverPort < 1 || serverPort > 65535) {
                throw new IllegalArgumentException(
                        "Port must be between 1 and 65535."
                );
            }

            // Pass the settings to the JavaFX client.
            Application.launch(ChatClientApp.class,
                    "--server-host=" + serverHost,
                    "--server-port=" + serverPort);

        } catch (NumberFormatException e) {
            System.err.println(
                    "Invalid port. Usage: java TCPClient <ServerIPAddress> <PortNumber>"
            );
        } catch (IllegalArgumentException e) {
            System.err.println("Configuration error: " + e.getMessage());
        }
    }
}