package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class ServerDashboard extends Application {

    private TextArea activityLog;
    private ListView<String> usersList;
    private Label statusLabel;

    private ChatServer chatServer;

    private final ConcurrentHashMap<String, String> userColors =
            new ConcurrentHashMap<>();

    @Override
    public void start(Stage stage) {

        Label title = new Label("TCP Chat Server");
        title.setStyle(
                "-fx-font-size: 24px; -fx-font-weight: bold;"
        );

        statusLabel = new Label("Starting server...");

        activityLog = new TextArea();
        activityLog.setEditable(false);
        activityLog.setWrapText(true);

        usersList = new ListView<>();
        usersList.setPlaceholder(
                new Label("No connected users")
        );
        usersList.setPrefWidth(220);
        usersList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(String username, boolean empty) {
                super.updateItem(username, empty);

                if (empty || username == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(username);
                    String color = userColors.computeIfAbsent(
                            username, ignored -> randomColor()
                    );

                    setStyle(
                            "-fx-background-color: " + color + ";"
                                    + "-fx-padding: 8;"
                    );
                }
            }
        });

        VBox logPanel = new VBox(
                8, new Label("Server Activity"), activityLog
        );

        VBox usersPanel = new VBox(
                8, new Label("Connected Users"), usersList
        );

        VBox.setVgrow(activityLog, Priority.ALWAYS);
        VBox.setVgrow(usersList, Priority.ALWAYS);

        HBox content = new HBox(15, logPanel, usersPanel);
        HBox.setHgrow(logPanel, Priority.ALWAYS);

        BorderPane layout = new BorderPane();
        layout.setTop(new VBox(10, title, statusLabel));
        layout.setCenter(content);
        layout.setPadding(new Insets(20));

        BorderPane.setMargin(content, new Insets(20, 0, 0, 0));

        Scene scene = new Scene(layout, 850, 500);

        stage.setTitle("TCP Server Dashboard");
        stage.setScene(scene);
        stage.setMinWidth(650);
        stage.setMinHeight(400);

        stage.setOnCloseRequest(event -> {
            if (chatServer != null) {
                appendLog(
                        "Dashboard closed. Stop the server process if it remains running."
                );
            }
        });

        stage.show();

        startServer();
    }

    private void startServer() {
        try {
            ServerConfig config = new ServerConfig();
            chatServer = new ChatServer(config);

            chatServer.setEventListeners(
                    this::appendLog,
                    this::updateUsers
            );

            Thread.startVirtualThread(chatServer::start);

        } catch (IOException e) {
            statusLabel.setText("Could not load server configuration.");
            appendLog("Configuration error: " + e.getMessage());
        }
    }

    private void appendLog(String message) {
        Platform.runLater(() -> {
            activityLog.appendText(message + "\n");

            if (message.startsWith("Server started on")) {
                statusLabel.setText("Server running");
            } else if (message.startsWith("Server error:")) {
                statusLabel.setText("Server error");
            }
        });
    }

    private void updateUsers(List<String> usernames) {
        Platform.runLater(() -> {
            usersList.getItems().setAll(usernames);

            for (String username : usernames) {
                userColors.computeIfAbsent(
                        username, ignored -> randomColor()
                );
            }

            usersList.refresh();
        });
    }

    private String randomColor() {
        int red = ThreadLocalRandom.current().nextInt(180, 256);
        int green = ThreadLocalRandom.current().nextInt(180, 256);
        int blue = ThreadLocalRandom.current().nextInt(180, 256);

        return String.format("#%02X%02X%02X", red, green, blue);
    }

    public static void main(String[] args) {
        launch(args);
    }
}