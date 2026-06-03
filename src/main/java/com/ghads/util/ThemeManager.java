package com.ghads.util;

import javafx.scene.Scene;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages application-wide theme (light/dark) and font preferences.
 * All registered scenes are updated when preferences change.
 */
public class ThemeManager {

    public enum Theme { LIGHT, DARK }

    private static Theme currentTheme = Theme.LIGHT;
    private static String currentFontFamily = "Segoe UI";
    private static int currentFontSize = 13;

    private static final List<Scene> registeredScenes = new ArrayList<>();

    private ThemeManager() {}

    public static void registerScene(Scene scene) {
        if (!registeredScenes.contains(scene)) {
            registeredScenes.add(scene);
        }
        applyTheme(scene);
        applyFont(scene);
    }

    public static void setTheme(Theme theme) {
        currentTheme = theme;
        registeredScenes.forEach(ThemeManager::applyTheme);
    }

    public static void toggleTheme() {
        setTheme(currentTheme == Theme.LIGHT ? Theme.DARK : Theme.LIGHT);
    }

    public static Theme getCurrentTheme() { return currentTheme; }

    public static void setFontFamily(String family) {
        currentFontFamily = family;
        registeredScenes.forEach(ThemeManager::applyFont);
    }

    public static void setFontSize(int size) {
        currentFontSize = size;
        registeredScenes.forEach(ThemeManager::applyFont);
    }

    private static void applyTheme(Scene scene) {
        scene.getStylesheets().removeIf(s -> s.contains("dark") || s.contains("light"));
        String cssPath = currentTheme == Theme.DARK
            ? ThemeManager.class.getResource("/com/ghads/css/dark.css").toExternalForm()
            : ThemeManager.class.getResource("/com/ghads/css/light.css").toExternalForm();
        scene.getStylesheets().add(cssPath);
    }

    private static void applyFont(Scene scene) {
        // Inject font via inline style on root
        scene.getRoot().setStyle(
            "-fx-font-family: '" + currentFontFamily + "'; " +
            "-fx-font-size: " + currentFontSize + "px;"
        );
    }

    public static String getCurrentFontFamily() { return currentFontFamily; }
    public static int getCurrentFontSize()       { return currentFontSize; }
}
