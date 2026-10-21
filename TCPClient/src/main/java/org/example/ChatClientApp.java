
package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class ChatClientApp extends Application {

    private ClientConnection client;

    private String serverHost;
    private int serverPort;
    private String username = "";

    private Label statusLabel;
    private Label connectionIndicator;
    private TextArea chatArea;
    private ListView<String> usersList;
    private TextField usernameField;
    private TextField messageField;
    private Button connectButton;
    private Button sendButton;

    private volatile boolean connected;
    private volatile boolean readOnly;
    private boolean closing;

    @Override
    public void start(Stage stage) {
        try {
            loadConfiguration();
            applyCommandLineArguments();
        } catch (IOException | IllegalArgumentException e) {
            showError("Configuration Error", e.getMessage());
            Platform.exit();
            return;
        }

        Label title = new Label("TCP Chat Application");
        title.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");

        usernameField = new TextField();
        usernameField.setPromptText("Enter your username");
        usernameField.setPrefWidth(260);

        connectButton = new Button("Connect");
        connectButton.setOnAction(event -> connectToServer());

        HBox connectionRow = new HBox(10, usernameField, connectButton);
        connectionRow.setAlignment(Pos.CENTER_LEFT);

        connectionIndicator = new Label("●");
        connectionIndicator.setStyle("-fx-text-fill: #dc2626;");

        statusLabel = new Label("Offline");

        HBox statusRow = new HBox(8, connectionIndicator, statusLabel);
        statusRow.setAlignment(Pos.CENTER_LEFT);

        chatArea = new TextArea();
        chatArea.setEditable(false);
        chatArea.setWrapText(true);
        chatArea.setPromptText("Chat messages will appear here.");

        usersList = new ListView<>();
        usersList.setPlaceholder(new Label("No connected users"));

        usersList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(String user, boolean empty) {
                super.updateItem(user, empty);

                if (empty || user == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(user);

                    // Stable, user-specific background color.
                    int hash = user.hashCode();
                    int red = 180 + Math.floorMod(hash, 60);
                    int green = 180 + Math.floorMod(hash / 7, 60);
                    int blue = 180 + Math.floorMod(hash / 13, 60);

                    setStyle(String.format(
                            "-fx-background-color: rgb(%d,%d,%d);",
                            red, green, blue
                    ));
                }
            }
        });

        Label usersHeading = new Label("Active Users");
        VBox usersPanel = new VBox(8, usersHeading, usersList);
        usersPanel.setPrefWidth(250);
        VBox.setVgrow(usersList, Priority.ALWAYS);

        HBox chatPanel = new HBox(15, chatArea, usersPanel);
        HBox.setHgrow(chatArea, Priority.ALWAYS);
        VBox.setVgrow(chatPanel, Priority.ALWAYS);

        messageField = new TextField();
        messageField.setPromptText("Type your message...");
        messageField.setDisable(true);
        messageField.setOnAction(event -> sendMessage());

        sendButton = new Button("Send");
        sendButton.setDisable(true);
        sendButton.setOnAction(event -> sendMessage());

        HBox messageRow = new HBox(10, messageField, sendButton);
        HBox.setHgrow(messageField, Priority.ALWAYS);

        VBox root = new VBox(
                20,
                title,
                connectionRow,
                statusRow,
                chatPanel,
                messageRow
        );

        root.setPadding(new Insets(25));
        root.setStyle("-fx-background-color: #f3f6fb;");

        Scene scene = new Scene(root, 900, 600);

        stage.setTitle("TCP Chat Client");
        stage.setScene(scene);
        stage.setMinWidth(650);
        stage.setMinHeight(450);
        stage.setOnCloseRequest(event -> {
            closing = true;
            disconnectFromServer();
        });

        stage.show();
    }

    private void loadConfiguration() throws IOException {
        Properties properties = new Properties();
        Path externalConfig = Path.of("config", "client.properties");

        if (Files.isRegularFile(externalConfig)) {
            try (InputStream input =
                         Files.newInputStream(externalConfig)) {
                properties.load(input);
            }
        } else {
            try (InputStream input = getClass()
                    .getClassLoader()
                    .getResourceAsStream("client.properties")) {

                if (input == null) {
                    throw new IOException(
                            "client.properties was not found. Create it "
                                    + "in config/ or src/main/resources/."
                    );
                }

                properties.load(input);
            }
        }

        serverHost = properties
                .getProperty("server.host", "localhost")
                .trim();

        String portValue = properties
                .getProperty("server.port", "3000")
                .trim();

        if (serverHost.isBlank()) {
            throw new IOException("server.host cannot be empty.");
        }

        try {
            serverPort = Integer.parseInt(portValue);
        } catch (NumberFormatException e) {
            throw new IOException(
                    "server.port must be a valid number.", e
            );
        }

        validatePort(serverPort);
    }

    private void applyCommandLineArguments() {
        var arguments = getParameters().getRaw();

        if (arguments.isEmpty()) {
            return; // Use the configuration file.
        }

        if (arguments.size() != 2) {
            throw new IllegalArgumentException(
                    "Usage: TCPClient <ServerIPAddress> <PortNumber>"
            );
        }

        serverHost = arguments.get(0).trim();

        if (serverHost.isBlank()) {
            throw new IllegalArgumentException(
                    "Server IP address or hostname cannot be empty."
            );
        }

        try {
            serverPort = Integer.parseInt(arguments.get(1));
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Port must be a valid integer."
            );
        }

        validatePort(serverPort);
    }

    private void validatePort(int port) {
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException(
                    "Port must be between 1 and 65535."
            );
        }
    }

    private void connectToServer() {
        if (connected) {
            return;
        }

        username = usernameField.getText().trim();
        readOnly = username.isEmpty();

        try {
            chatArea.clear();
            usersList.getItems().clear();

            appendMessage(
                    "Connecting to " + serverHost + ":" + serverPort + "..."
            );

            client = new ClientConnection();

            client.connect(
                    serverHost,
                    serverPort,
                    username,
                    this::handleServerMessage
            );

            connected = true;

            usernameField.setDisable(true);
            connectButton.setDisable(true);
            messageField.setDisable(readOnly);
            sendButton.setDisable(readOnly);

            statusLabel.setText(
                    readOnly
                            ? "Connected — read-only guest"
                            : "Online — " + serverHost + ":" + serverPort
            );
            connectionIndicator.setStyle("-fx-text-fill: #16a34a;");

            if (!readOnly) {
                messageField.requestFocus();
            }

            client.sendMessage("allUsers");

        } catch (IOException e) {
            connected = false;
            statusLabel.setText("Connection failed");
            connectionIndicator.setStyle("-fx-text-fill: #dc2626;");
            appendMessage("Could not connect: " + e.getMessage());

            if (client != null) {
                client.disconnect();
                client = null;
            }

            usernameField.setDisable(false);
            connectButton.setDisable(false);
            messageField.setDisable(true);
            sendButton.setDisable(true);
        }
    }

    private void handleServerMessage(String message) {
        Platform.runLater(() -> {
            appendMessage(message);

            if (message.startsWith("Active users:")) {
                updateUsersList(
                        message.substring("Active users:".length()).trim()
                );
            }

            if (message.startsWith("READ-ONLY MODE:")) {
                readOnly = true;
                messageField.setDisable(true);
                sendButton.setDisable(true);
                statusLabel.setText("Connected — read-only guest");
            }

            if (message.contains(" joined the chat.")
                    || message.contains(" left the chat.")) {
                if (connected && client != null && !readOnly) {
                    client.sendMessage("allUsers");
                }
            }

            if (message.equals("Disconnected from server.")) {
                connected = false;
                statusLabel.setText("Disconnected");
                connectionIndicator.setStyle("-fx-text-fill: #dc2626;");

                usernameField.setDisable(false);
                connectButton.setDisable(false);
                messageField.setDisable(true);
                sendButton.setDisable(true);
            }
        });
    }

    private void updateUsersList(String users) {
        usersList.getItems().clear();

        if (users.isBlank()
                || users.equalsIgnoreCase("No users connected")) {
            return;
        }

        for (String user : users.split(",\\s*")) {
            if (!user.isBlank()) {
                usersList.getItems().add(user);
            }
        }
    }

    private void sendMessage() {
        if (!connected || client == null || readOnly) {
            return;
        }

        String message = messageField.getText().trim();

        if (message.isEmpty()) {
            return;
        }

        if (message.equalsIgnoreCase("bye")
                || message.equalsIgnoreCase("end")) {
            disconnectFromServer();
            statusLabel.setText("Disconnected");
            connectionIndicator.setStyle("-fx-text-fill: #dc2626;");
            usernameField.setDisable(false);
            connectButton.setDisable(false);
            messageField.setDisable(true);
            sendButton.setDisable(true);
            return;
        }

        client.sendMessage(message);
        messageField.clear();
    }

    private void disconnectFromServer() {
        connected = false;

        if (client != null) {
            client.disconnect();
            client = null;
        }

        if (!closing && statusLabel != null) {
            statusLabel.setText("Disconnected");
            connectionIndicator.setStyle("-fx-text-fill: #dc2626;");
            usernameField.setDisable(false);
            connectButton.setDisable(false);
            messageField.setDisable(true);
            sendButton.setDisable(true);
        }
    }

    private void appendMessage(String message) {
        chatArea.appendText(message + System.lineSeparator());
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
