
package org.example;

import java.io.IOException;

public class Main {

    public static void main(String[] args) {
        try {
            ServerConfig config = new ServerConfig();

            ChatServer server = new ChatServer(config);

            server.start();

        } catch (IOException e) {
            System.err.println(
                    "Could not load server configuration: "
                            + e.getMessage()
            );
        }
    }
}
