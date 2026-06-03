package com.ghads.util;

import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextInputControl;

import java.util.ArrayList;
import java.util.List;

/**
 * Centralised validation utility for all GHADS forms.
 * Returns a list of error messages; empty list = all valid.
 */
public class ValidationUtil {

    private ValidationUtil() {}

    // ── Field validators ──────────────────────────────────────────────────────

    public static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[\\w.+\\-]+@[\\w\\-]+(\\.[\\w\\-]+)+$");
    }

    public static boolean isValidPassword(String password) {
        return password != null && password.length() >= 8;
    }

    public static boolean isPositiveInteger(String value) {
        try {
            return Integer.parseInt(value.trim()) > 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // ── Form-level validators ─────────────────────────────────────────────────

    /**
     * Validates that a TextInputControl is not empty.
     * Highlights the field red on failure.
     */
    public static boolean requireNonBlank(TextInputControl field, String label, List<String> errors) {
        if (isBlank(field.getText())) {
            errors.add(label + " is required.");
            field.getStyleClass().add("field-error");
            return false;
        }
        field.getStyleClass().remove("field-error");
        return true;
    }

    public static boolean requireValidEmail(TextInputControl field, List<String> errors) {
        if (!isValidEmail(field.getText().trim())) {
            errors.add("Email address is not valid.");
            field.getStyleClass().add("field-error");
            return false;
        }
        field.getStyleClass().remove("field-error");
        return true;
    }

    public static boolean requirePassword(TextInputControl field, List<String> errors) {
        if (!isValidPassword(field.getText())) {
            errors.add("Password must be at least 8 characters.");
            field.getStyleClass().add("field-error");
            return false;
        }
        field.getStyleClass().remove("field-error");
        return true;
    }

    public static boolean requirePositiveInt(TextInputControl field, String label, List<String> errors) {
        if (!isPositiveInteger(field.getText())) {
            errors.add(label + " must be a positive number.");
            field.getStyleClass().add("field-error");
            return false;
        }
        field.getStyleClass().remove("field-error");
        return true;
    }

    public static <T> boolean requireSelected(ComboBox<T> combo, String label, List<String> errors) {
        if (combo.getValue() == null) {
            errors.add(label + " must be selected.");
            combo.getStyleClass().add("field-error");
            return false;
        }
        combo.getStyleClass().remove("field-error");
        return true;
    }

    public static boolean requireDatePicked(DatePicker dp, String label, List<String> errors) {
        if (dp.getValue() == null) {
            errors.add(label + " must be selected using the date picker.");
            dp.getStyleClass().add("field-error");
            return false;
        }
        dp.getStyleClass().remove("field-error");
        return true;
    }

    // ── Error display helper ──────────────────────────────────────────────────

    /**
     * Joins all errors and shows them in a single alert.
     * Returns false if there are errors, true if the list is empty (all valid).
     */
    public static boolean showErrors(List<String> errors) {
        if (errors.isEmpty()) return true;
        AlertHelper.showError("Validation Errors", String.join("\n", errors));
        return false;
    }

    /** Convenience: clear all field-error styles from a list of fields. */
    public static void clearErrors(TextInputControl... fields) {
        for (TextInputControl f : fields) {
            f.getStyleClass().remove("field-error");
        }
    }

    public static List<String> newErrorList() {
        return new ArrayList<>();
    }
}
