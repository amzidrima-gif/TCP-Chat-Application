
package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.function.Consumer;

public class ClientConnection {

    private Socket socket;
    private PrintWriter output;
    private BufferedReader input;

    public void connect(
            String host,
            int port,
            String username,
            Consumer<String> onMessage) throws IOException {

        socket = new Socket(host, port);

        output = new PrintWriter(
                socket.getOutputStream(), true);

        input = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));

        // Send the username as the first line.
        output.println(username);

        // Read incoming messages on a separate virtual thread.
        Thread.startVirtualThread(() -> {
            try {
                String message;

                while ((message = input.readLine()) != null) {
                    onMessage.accept(message);
                }

            } catch (IOException e) {
                onMessage.accept("Disconnected from server.");
            }
        });
    }

    public void sendMessage(String message) {
        if (output != null && !socket.isClosed()) {
            output.println(message);
        }
    }

    public void disconnect() {
        try {
            if (socket != null && !socket.isClosed()) {
                output.println("bye");
                socket.close();
            }
        } catch (IOException e) {
            System.err.println(
                    "Error closing connection: " + e.getMessage());
        }
    }
}
