package com.ghads.util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Utility for switching scenes in the primary stage.
 */
public class SceneManager {

    private static Stage primaryStage;

    private SceneManager() {}

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static void switchScene(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(
                SceneManager.class.getResource(fxmlPath)
            );
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(
                SceneManager.class.getResource("/com/ghads/css/style.css").toExternalForm()
            );
            primaryStage.setTitle("GHADS – " + title);
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
            primaryStage.show();
        } catch (IOException e) {
            AlertHelper.showError("Navigation Error", "Cannot load screen: " + fxmlPath + "\n" + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Load FXML and return the loader (for accessing the controller).
     */
    public static FXMLLoader getLoader(String fxmlPath) {
        return new FXMLLoader(SceneManager.class.getResource(fxmlPath));
    }
}
