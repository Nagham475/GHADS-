package com.ghads.controller;

import com.ghads.util.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class AdminNavigationController extends BaseController {

    @FXML
    protected void onNavDashboard(ActionEvent e) {
        SceneManager.switchScene("/com/ghads/view/admin/AdminDashboard.fxml", "Admin Dashboard");
    }

    @FXML
    protected void onNavOrganizations(ActionEvent e) {
        SceneManager.switchScene("/com/ghads/view/admin/Organizations.fxml", "Organizations");
    }

    @FXML
    protected void onNavUsers(ActionEvent e) {
        SceneManager.switchScene("/com/ghads/view/admin/Users.fxml", "Users");
    }

    @FXML
    protected void onNavFamilies(ActionEvent e) {
        SceneManager.switchScene("/com/ghads/view/admin/Families.fxml", "Families");
    }

    @FXML
    protected void onNavDistributions(ActionEvent e) {
        SceneManager.switchScene("/com/ghads/view/admin/Distributions.fxml", "Aid Distributions");
    }

    @FXML
    protected void onNavChangePassword(ActionEvent e) {
        SceneManager.switchScene("/com/ghads/view/admin/ChangePassword.fxml", "Change Password");
    }
}
