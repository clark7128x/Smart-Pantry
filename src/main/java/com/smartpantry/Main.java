// src/main/java/com/smartpantry/Main.java
package com.smartpantry;

import com.smartpantry.dao.DatabaseManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

/**
 * Main entry point for the Smart Pantry application.
 */
public class Main extends Application {

    private static final int WINDOW_WIDTH = 1200;
    private static final int WINDOW_HEIGHT = 750;

    public static void main(String[] args) {
        // Initialize database before launching the application
        DatabaseManager.getInstance().initializeDatabase();
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            // Debug: Print resource locations
            System.out.println("Looking for FXML files...");

            URL fxmlUrl = getClass().getResource("/views/MainView.fxml");
            URL cssUrl = getClass().getResource("/styles/application.css");

            System.out.println("FXML URL: " + fxmlUrl);
            System.out.println("CSS URL: " + cssUrl);

            if (fxmlUrl == null) {
                System.err.println("ERROR: Cannot find /views/MainView.fxml");
                System.err.println("Make sure the file exists in: src/main/resources/views/MainView.fxml");

                // Try alternative paths
                fxmlUrl = getClass().getClassLoader().getResource("views/MainView.fxml");
                System.out.println("Trying alternative path: " + fxmlUrl);

                if (fxmlUrl == null) {
                    throw new IOException("Cannot find MainView.fxml. Check your resources folder structure.");
                }
            }

            // Load the main view from FXML
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Parent root = loader.load();

            // Create the scene
            Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);

            // Load CSS if available
            if (cssUrl != null) {
                scene.getStylesheets().add(cssUrl.toExternalForm());
            } else {
                System.out.println("Warning: CSS file not found, using default styles");
            }

            // Configure the primary stage
            primaryStage.setTitle("Smart Pantry - Recipe & Inventory Manager");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(900);
            primaryStage.setMinHeight(600);
            primaryStage.show();

            System.out.println("Application started successfully!");

        } catch (IOException e) {
            System.err.println("Error loading application: " + e.getMessage());
            e.printStackTrace();

            // Show error dialog
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR
            );
            alert.setTitle("Application Error");
            alert.setHeaderText("Failed to load application");
            alert.setContentText("Error: " + e.getMessage() +
                    "\n\nMake sure FXML files exist in:\nsrc/main/resources/views/");
            alert.showAndWait();
        }
    }

    @Override
    public void stop() {
        DatabaseManager.getInstance().closeConnection();
        System.out.println("Application closed. Database connection released.");
    }
}