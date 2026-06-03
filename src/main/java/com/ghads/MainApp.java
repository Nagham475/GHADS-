package com.ghads;
/**
 * ============================================================
 * Gaza Humanitarian Aid Distribution System (GHADS)
 * ============================================================
 *
 * @authors  نغم نعيم قنوع       - رقم جامعي: 220223055
 *           صباح صلاح فدعوس    - رقم جامعي: 220222361
 *

 */
import com.ghads.config.DatabaseConnection;
import com.ghads.util.SceneManager;
import javafx.application.Application;
import javafx.stage.Stage;

   
public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("GHADS – Gaza Humanitarian Aid Distribution System");
        primaryStage.setMinWidth(900);
        primaryStage.setMinHeight(600);
        primaryStage.setResizable(true);

        SceneManager.setPrimaryStage(primaryStage);

                             
        // Open the login screen
        SceneManager.switchScene("/com/ghads/view/Login.fxml", "Login");
    }

    @Override
    public void stop() {
        // Close DB connection gracefully on exit
        DatabaseConnection.getInstance().closeConnection();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
