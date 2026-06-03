package com.ghads.controller;

import com.ghads.dao.UserDAO;
import com.ghads.dao.impl.UserDAOImpl;
import com.ghads.model.User;
import com.ghads.util.AlertHelper;
import com.ghads.util.SceneManager;
import com.ghads.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for Login.fxml
 * Handles authentication and redirects to the correct dashboard based on role.
 */
public class LoginController implements Initializable {

    @FXML private TextField     txfUsername;
    @FXML private PasswordField txfPassword;
    @FXML private Label         lblError;

    private final UserDAO userDAO = new UserDAOImpl();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblError.setVisible(false);
    }

    @FXML
    private void onLogin(ActionEvent event) {
        String username = txfUsername.getText().trim();
        String password = txfPassword.getText().trim();

        // Basic input validation
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password.");
            return;
        }

        // Authenticate
        Optional<User> result = userDAO.authenticate(username, password);

        if (result.isEmpty()) {
            showError("Incorrect username or password. Please try again.");
            txfPassword.clear();
            return;
        }

        User user = result.get();
        SessionManager.setCurrentUser(user);
        lblError.setVisible(false);

        // Role-based redirection
        if (user.isAdmin()) {
            SceneManager.switchScene("/com/ghads/view/admin/AdminDashboard.fxml", "Admin Dashboard");
        } else if (user.isCoordinator()) {
            SceneManager.switchScene("/com/ghads/view/coordinator/CoordDashboard.fxml",
                    "Coordinator Dashboard – " + user.getOrganizationName());
        } else {
            showError("Unknown role: " + user.getRole());
        }
    }

    @FXML
    private void onClear(ActionEvent event) {
        txfUsername.clear();
        txfPassword.clear();
        lblError.setVisible(false);
        txfUsername.requestFocus();
    }

    private void showError(String message) {
        lblError.setText(message);
        lblError.setVisible(true);
    }
}
