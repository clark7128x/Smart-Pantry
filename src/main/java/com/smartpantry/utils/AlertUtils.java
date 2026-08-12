// src/main/java/com/smartpantry/utils/AlertUtils.java
package com.smartpantry.utils;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Utility class for displaying various types of alerts and dialogs.
 * Provides consistent styling and behavior across the application.
 *
 * @author Smart Pantry Team
 * @version 1.0.0
 */
public class AlertUtils {

    /**
     * Private constructor to prevent instantiation.
     */
    private AlertUtils() {}

    /**
     * Shows an information alert.
     *
     * @param title the alert title
     * @param header the header text (can be null)
     * @param content the content text
     */
    public static void showInfo(String title, String header, String content) {
        Alert alert = createAlert(AlertType.INFORMATION, title, header, content);
        alert.showAndWait();
    }

    /**
     * Shows a warning alert.
     *
     * @param title the alert title
     * @param header the header text (can be null)
     * @param content the content text
     */
    public static void showWarning(String title, String header, String content) {
        Alert alert = createAlert(AlertType.WARNING, title, header, content);
        alert.showAndWait();
    }

    /**
     * Shows an error alert.
     *
     * @param title the alert title
     * @param header the header text (can be null)
     * @param content the content text
     */
    public static void showError(String title, String header, String content) {
        Alert alert = createAlert(AlertType.ERROR, title, header, content);
        alert.showAndWait();
    }

    /**
     * Shows a confirmation dialog and returns the user's choice.
     *
     * @param title the dialog title
     * @param header the header text (can be null)
     * @param content the content text
     * @return true if the user clicked OK, false otherwise
     */
    public static boolean showConfirmation(String title, String header, String content) {
        Alert alert = createAlert(AlertType.CONFIRMATION, title, header, content);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /**
     * Shows an error alert with exception details.
     *
     * @param title the alert title
     * @param header the header text
     * @param content the content text
     * @param ex the exception to display
     */
    public static void showException(String title, String header, String content, Exception ex) {
        Alert alert = createAlert(AlertType.ERROR, title, header, content);

        // Create expandable exception text area
        TextArea textArea = new TextArea(getStackTrace(ex));
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(textArea, 0, 0);

        alert.getDialogPane().setExpandableContent(expContent);
        alert.showAndWait();
    }

    /**
     * Creates a styled alert.
     *
     * @param type the alert type
     * @param title the alert title
     * @param header the header text
     * @param content the content text
     * @return the configured Alert object
     */
    private static Alert createAlert(AlertType type, String title, String header, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);

        // Apply styling
        alert.getDialogPane().getStylesheets().add(
                AlertUtils.class.getResource("/styles/application.css").toExternalForm()
        );

        return alert;
    }

    /**
     * Gets the stack trace of an exception as a string.
     *
     * @param ex the exception
     * @return the stack trace string
     */
    private static String getStackTrace(Exception ex) {
        StringBuilder sb = new StringBuilder();
        sb.append(ex.toString()).append("\n\n");
        for (StackTraceElement element : ex.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
        }
        return sb.toString();
    }
}