package com.ghads.controller;

import com.ghads.util.SceneManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

/**
 * Provides all coordinator sidebar navigation @FXML handlers.
 * Coordinator FXML controllers extend this class.
 */
public class CoordNavigationController extends BaseController {

    @FXML
    protected void onNavDashboard(ActionEvent e) {
        SceneManager.switchScene("/com/ghads/view/coordinator/CoordDashboard.fxml", "Coordinator Dashboard");
    }

    @FXML
    protected void onNavFamilies(ActionEvent e) {
        SceneManager.switchScene("/com/ghads/view/coordinator/CoordFamilies.fxml", "Family Management");
    }

    @FXML
    protected void onNavDistribution(ActionEvent e) {
        SceneManager.switchScene("/com/ghads/view/coordinator/CoordDistribution.fxml", "Aid Distribution");
    }

    @FXML
    protected void onNavProfile(ActionEvent e) {
        SceneManager.switchScene("/com/ghads/view/coordinator/CoordProfile.fxml", "My Profile");
    }

    @FXML
    protected void onNavChangePassword(ActionEvent e) {
        SceneManager.switchScene("/com/ghads/view/coordinator/CoordChangePassword.fxml", "Change Password");
    }
}
