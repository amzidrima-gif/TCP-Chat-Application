package org.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class ChatServer {

    private final ServerConfig config;

    private final ConcurrentHashMap<Socket, ClientHandler> clients =
            new ConcurrentHashMap<>();

    private final AtomicInteger guestCounter = new AtomicInteger(1);

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm:ss");

    // Dashboard event listeners. By default, the server logs to the console.
    private Consumer<String> logListener = message -> {};
    private Consumer<List<String>> usersListener = users -> {};

    public ChatServer(ServerConfig config) {
        this.config = config;
    }

    public void setEventListeners(
            Consumer<String> logListener,
            Consumer<List<String>> usersListener) {

        this.logListener = logListener != null
                ? logListener : message -> {};

        this.usersListener = usersListener != null
                ? usersListener : users -> {};
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket()) {

            serverSocket.bind(new InetSocketAddress(
                    config.getHost(), config.getPort()));

            log("Server started on "
                    + config.getHost() + ":" + config.getPort());

            while (true) {
                log("Waiting for client...");

                Socket socket = serverSocket.accept();

                // Each client is handled independently.
                Thread.startVirtualThread(() -> handleClient(socket));
            }

        } catch (IOException e) {
            log("Server error: " + e.getMessage());
        }
    }

    private void handleClient(Socket socket) {
        ClientHandler client = null;

        try {
            BufferedReader input = new BufferedReader(
                    new InputStreamReader(socket.getInputStream()));

            PrintWriter output = new PrintWriter(
                    socket.getOutputStream(), true);

            String requestedUsername = input.readLine();

            if (requestedUsername == null) {
                socket.close();
                return;
            }

            boolean readOnly = requestedUsername.isBlank();

            String username = readOnly
                    ? "Guest-" + guestCounter.getAndIncrement()
                    : requestedUsername.trim();

            client = new ClientHandler(
                    socket, username, output, readOnly);

            clients.put(socket, client);
            notifyUsers();

            output.println(readOnly
                    ? "READ-ONLY MODE: You cannot send messages."
                    : "Welcome " + username + "!");

            log("Welcome " + username);
            broadcast(username + " joined the chat.");

            String message;

            while ((message = input.readLine()) != null) {
                message = message.trim();

                if (message.equalsIgnoreCase("end")
                        || message.equalsIgnoreCase("bye")) {
                    break;
                }

                if (message.isEmpty()) {
                    continue;
                }

                if (message.equals("allUsers")) {
                    output.println("Active users: "
                            + getActiveUsers());
                    continue;
                }

                if (client.readOnly) {
                    output.println(
                            "READ-ONLY MODE: Message not sent.");
                    continue;
                }

                String formattedMessage = "["
                        + LocalTime.now().format(TIME_FORMAT)
                        + "] " + username + ": " + message;

                broadcast(formattedMessage);
                log(formattedMessage);
            }

        } catch (IOException e) {
            log("Connection issue: " + e.getMessage());

        } finally {
            if (client != null) {
                clients.remove(socket);
                notifyUsers();

                broadcast(client.username + " left the chat.");
                log(client.username + " disconnected.");
            }

            try {
                socket.close();
            } catch (IOException ignored) {
                // The connection may already be closed.
            }
        }
    }

    private void broadcast(String message) {
        for (ClientHandler client : clients.values()) {
            client.output.println(message);
        }
    }

    private String getActiveUsers() {
        return getActiveUsernames().stream()
                .reduce((first, second) -> first + ", " + second)
                .orElse("No users connected");
    }

    public List<String> getActiveUsernames() {
        return clients.values().stream()
                .map(client -> client.username)
                .sorted()
                .toList();
    }

    private void notifyUsers() {
        usersListener.accept(getActiveUsernames());
    }

    private void log(String message) {
        System.out.println(message);
        logListener.accept(message);
    }

    private static class ClientHandler {
        private final Socket socket;
        private final String username;
        private final PrintWriter output;
        private final boolean readOnly;

        private ClientHandler(
                Socket socket,
                String username,
                PrintWriter output,
                boolean readOnly) {

            this.socket = socket;
            this.username = username;
            this.output = output;
            this.readOnly = readOnly;
        }
    }
}