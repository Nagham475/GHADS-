package com.ghads.util;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.scene.control.DialogPane;

import java.util.Optional;

/**
 * Centralized alert/dialog helper used across all controllers.
 */
public class AlertHelper {

    private AlertHelper() {}

    public static void showSuccess(String title, String message) {
        show(AlertType.INFORMATION, title, message);
    }

    public static void showError(String title, String message) {
        show(AlertType.ERROR, title, message);
    }

    public static void showWarning(String title, String message) {
        show(AlertType.WARNING, title, message);
    }

    public static void showInfo(String title, String message) {
        show(AlertType.INFORMATION, title, message);
    }

    /**
     * Returns true if the user clicks OK/Yes on the confirmation dialog.
     */
    public static boolean showConfirmation(String title, String message) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert);
        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    private static void show(AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        styleAlert(alert);
        alert.showAndWait();
    }

    private static void styleAlert(Alert alert) {
        DialogPane dp = alert.getDialogPane();
        dp.getStylesheets().add(
            AlertHelper.class.getResource("/com/ghads/css/style.css").toExternalForm()
        );
        dp.getStyleClass().add("custom-dialog");
    }
}
