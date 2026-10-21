package org.example;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ServerConfig {

    private final String host;
    private final int port;

    public ServerConfig() throws IOException {
        Properties properties = new Properties();

        // Prefer an external configuration file so settings can be
        // changed without rebuilding the application.
        Path externalConfig = Path.of("config", "server.properties");

        if (Files.isRegularFile(externalConfig)) {
            try (InputStream input = Files.newInputStream(externalConfig)) {
                properties.load(input);
            }
        } else {
            // Fall back to the configuration packaged in resources.
            try (InputStream input = getClass()
                    .getClassLoader()
                    .getResourceAsStream("server.properties")) {

                if (input == null) {
                    throw new IOException(
                            "server.properties was not found externally "
                                    + "or in application resources."
                    );
                }

                properties.load(input);
            }
        }

        host = properties.getProperty("server.host", "localhost").trim();

        String portValue = properties
                .getProperty("server.port", "3000")
                .trim();

        if (host.isEmpty()) {
            throw new IOException(
                    "Invalid server configuration: server.host cannot be empty."
            );
        }

        try {
            port = Integer.parseInt(portValue);
        } catch (NumberFormatException e) {
            throw new IOException(
                    "Invalid server configuration: server.port must be a number.",
                    e
            );
        }

        if (port < 1 || port > 65535) {
            throw new IOException(
                    "Invalid server configuration: server.port must be "
                            + "between 1 and 65535."
            );
        }
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}