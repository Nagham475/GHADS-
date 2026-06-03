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
import javafx.scene.control.PasswordField;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for ChangePassword.fxml and CoordChangePassword.fxml.
 * Navigation is role-aware.
 */
public class ChangePasswordController extends BaseController implements Initializable {

    @FXML private PasswordField txfCurrentPassword;
    @FXML private PasswordField txfNewPassword;
    @FXML private PasswordField txfConfirmPassword;

    private final UserDAO userDAO = new UserDAOImpl();

    @Override
    public void initialize(URL url, ResourceBundle rb) {}

    @FXML
    private void onChangePassword(ActionEvent event) {
        String current = txfCurrentPassword.getText();
        String newPwd  = txfNewPassword.getText();
        String confirm = txfConfirmPassword.getText();

        User loggedIn = SessionManager.getCurrentUser();

        if (current.isBlank() || newPwd.isBlank() || confirm.isBlank()) {
            AlertHelper.showError("Validation", "All password fields are required.");
            return;
        }
        if (!loggedIn.getPassword().equals(current)) {
            AlertHelper.showError("Incorrect Password", "Current password is incorrect.");
            txfCurrentPassword.clear();
            txfCurrentPassword.requestFocus();
            return;
        }
        if (newPwd.length() < 8) {
            AlertHelper.showError("Validation", "New password must be at least 8 characters.");
            return;
        }
        if (!newPwd.equals(confirm)) {
            AlertHelper.showError("Validation", "New passwords do not match.");
            txfConfirmPassword.clear();
            return;
        }
        if (newPwd.equals(current)) {
            AlertHelper.showWarning("Warning", "New password must be different from the current password.");
            return;
        }
        userDAO.updatePassword(loggedIn.getUserId(), newPwd);
        loggedIn.setPassword(newPwd);
        AlertHelper.showSuccess("Success", "Password changed successfully.");
        clearForm();
    }

    @FXML private void onClear(ActionEvent e) { clearForm(); }

    @FXML
    private void onBack(ActionEvent e) {
        if (SessionManager.isAdmin())
            SceneManager.switchScene("/com/ghads/view/admin/AdminDashboard.fxml", "Admin Dashboard");
        else
            SceneManager.switchScene("/com/ghads/view/coordinator/CoordDashboard.fxml", "Coordinator Dashboard");
    }

    // ── Sidebar navigation ────────────────────────────────────────────────────
    @FXML private void onNavDashboard(ActionEvent e)       { onBack(e); }
    @FXML private void onNavOrganizations(ActionEvent e) {
        SceneManager.switchScene("/com/ghads/view/admin/Organizations.fxml", "Organizations");
    }
    @FXML private void onNavUsers(ActionEvent e) {
        SceneManager.switchScene("/com/ghads/view/admin/Users.fxml", "Users");
    }
    @FXML private void onNavFamilies(ActionEvent e) {
        if (SessionManager.isAdmin())
            SceneManager.switchScene("/com/ghads/view/admin/Families.fxml", "Families");
        else
            SceneManager.switchScene("/com/ghads/view/coordinator/CoordFamilies.fxml", "Families");
    }
    @FXML private void onNavDistributions(ActionEvent e) {
        SceneManager.switchScene("/com/ghads/view/admin/Distributions.fxml", "Aid Distributions");
    }
    @FXML private void onNavDistribution(ActionEvent e) {
        SceneManager.switchScene("/com/ghads/view/coordinator/CoordDistribution.fxml", "Aid Distribution");
    }
    @FXML private void onNavProfile(ActionEvent e) {
        SceneManager.switchScene("/com/ghads/view/coordinator/CoordProfile.fxml", "My Profile");
    }
    @FXML private void onNavChangePassword(ActionEvent e) { /* already here */ }

    private void clearForm() {
        txfCurrentPassword.clear();
        txfNewPassword.clear();
        txfConfirmPassword.clear();
    }
}
