package com.ghads.controller;

import com.ghads.util.AlertHelper;
import com.ghads.util.SceneManager;
import com.ghads.util.SessionManager;
import com.ghads.util.ThemeManager;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

/**
 * Base controller providing shared Menu Bar behaviour for all screens.
 * All controllers should extend this class.
 */
public abstract class BaseController {

    // ── File menu ────────────────────────────────────────────────────────────

    @FXML
    protected void onMenuExit(ActionEvent event) {
        if (AlertHelper.showConfirmation("Exit", "Are you sure you want to exit GHADS?")) {
            Platform.exit();
        }
    }

    // ── Format menu ──────────────────────────────────────────────────────────

    @FXML
    protected void onFontSizeSmall(ActionEvent event)  { ThemeManager.setFontSize(11); }

    @FXML
    protected void onFontSizeMedium(ActionEvent event) { ThemeManager.setFontSize(13); }

    @FXML
    protected void onFontSizeLarge(ActionEvent event)  { ThemeManager.setFontSize(16); }

    @FXML
    protected void onFontSegoe(ActionEvent event)      { ThemeManager.setFontFamily("Segoe UI"); }

    @FXML
    protected void onFontArial(ActionEvent event)      { ThemeManager.setFontFamily("Arial"); }

    @FXML
    protected void onFontTahoma(ActionEvent event)     { ThemeManager.setFontFamily("Tahoma"); }

    @FXML
    protected void onFontCourier(ActionEvent event)    { ThemeManager.setFontFamily("Courier New"); }

    @FXML
    protected void onToggleTheme(ActionEvent event) {
        ThemeManager.toggleTheme();
        // Re-register current scene after theme change
        Scene scene = SceneManager.getPrimaryStage().getScene();
        if (scene != null) ThemeManager.registerScene(scene);
    }

    @FXML
    protected void onLightTheme(ActionEvent event) {
        ThemeManager.setTheme(ThemeManager.Theme.LIGHT);
    }

    @FXML
    protected void onDarkTheme(ActionEvent event) {
        ThemeManager.setTheme(ThemeManager.Theme.DARK);
    }

    // ── Help menu ────────────────────────────────────────────────────────────

    @FXML
    protected void onAboutApp(ActionEvent event) {
        AlertHelper.showInfo("About GHADS",
            "Gaza Humanitarian Aid Distribution System (GHADS)\n\n" +
            "Version: 1.0  |  2026\n\n" +
            "GHADS helps humanitarian organizations in Gaza coordinate\n" +
            "aid distribution for displaced families.\n\n" +
            "The system prevents duplicate aid distribution and ensures\n" +
            "every family in need is identified and served fairly.\n\n" +
            "Built with: Java 17, JavaFX 17, MySQL 8, Scene Builder\n" +
            "Architecture: MVC + DAO Pattern + Singleton\n\n" +
            "Instructor: Aya N. Alharazin\n" +
            "The Islamic University of Gaza – CSCI 2108"
        );
    }

    // ── Logout (shared) ──────────────────────────────────────────────────────

    @FXML
    protected void onLogout(ActionEvent event) {
        if (AlertHelper.showConfirmation("Logout", "Are you sure you want to logout?")) {
            SessionManager.clearSession();
            SceneManager.switchScene("/com/ghads/view/Login.fxml", "Login");
        }
    }
}
