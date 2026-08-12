// src/main/java/com/smartpantry/controllers/MainController.java
package com.smartpantry.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

/**
 * Main controller for the application.
 * Manages navigation between different views.
 */
public class MainController implements Initializable {

    @FXML private StackPane contentArea;
    @FXML private Button btnPantry;
    @FXML private Button btnRecipes;
    @FXML private Button btnWhatToCook;
    @FXML private Label lblDate;
    @FXML private VBox navButtons;

    private Button activeButton;
    private PantryController pantryController;
    private RecipeController recipeController;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        updateDateLabel();
        loadPantryView();
        setActiveButton(btnPantry);
    }

    @FXML
    private void handlePantryClick() {
        loadPantryView();
        setActiveButton(btnPantry);
    }

    @FXML
    private void handleRecipesClick() {
        loadRecipesView(RecipeController.ViewMode.MANAGE);
        setActiveButton(btnRecipes);
    }

    @FXML
    private void handleWhatToCookClick() {
        loadRecipesView(RecipeController.ViewMode.SUGGESTIONS);
        setActiveButton(btnWhatToCook);
    }

    private void loadPantryView() {
        try {
            URL fxmlUrl = getClass().getResource("/views/PantryView.fxml");
            if (fxmlUrl == null) {
                showErrorInContentArea("Cannot find PantryView.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Node pantryView = loader.load();
            pantryController = loader.getController();
            contentArea.getChildren().setAll(pantryView);

        } catch (IOException e) {
            System.err.println("Error loading Pantry view: " + e.getMessage());
            e.printStackTrace();
            showErrorInContentArea("Error loading Pantry view: " + e.getMessage());
        }
    }

    private void loadRecipesView(RecipeController.ViewMode mode) {
        try {
            URL fxmlUrl = getClass().getResource("/views/RecipeView.fxml");
            if (fxmlUrl == null) {
                showErrorInContentArea("Cannot find RecipeView.fxml");
                return;
            }

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Node recipeView = loader.load();
            recipeController = loader.getController();
            recipeController.setMode(mode);
            contentArea.getChildren().setAll(recipeView);

        } catch (IOException e) {
            System.err.println("Error loading Recipe view: " + e.getMessage());
            e.printStackTrace();
            showErrorInContentArea("Error loading Recipe view: " + e.getMessage());
        }
    }

    private void showErrorInContentArea(String message) {
        Label errorLabel = new Label("⚠️ " + message);
        errorLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #e74c3c;");
        contentArea.getChildren().setAll(errorLabel);
    }

    private void setActiveButton(Button button) {
        // Remove active style from previous button
        if (activeButton != null) {
            activeButton.setStyle(activeButton.getStyle().replace(
                    "-fx-background-color: #3498db;",
                    "-fx-background-color: transparent;"));
        }

        // Add active style to new button
        button.setStyle(button.getStyle().replace(
                "-fx-background-color: transparent;",
                "-fx-background-color: #3498db;"));
        activeButton = button;
    }

    private void updateDateLabel() {
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
        lblDate.setText(today.format(formatter));
    }
}