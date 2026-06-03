package com.ghads.controller;

import com.ghads.dao.AidDistributionDAO;
import com.ghads.dao.FamilyDAO;
import com.ghads.dao.impl.AidDistributionDAOImpl;
import com.ghads.dao.impl.FamilyDAOImpl;
import com.ghads.util.SceneManager;
import com.ghads.util.SessionManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for CoordDashboard.fxml
 */
public class CoordDashboardController extends BaseController implements Initializable {

    @FXML private Label lblWelcome;
    @FXML private Label lblOrgName;
    @FXML private Label lblTotalFamilies;
    @FXML private Label lblServedByOrg;
    @FXML private Label lblUnservedFamilies;

    private final FamilyDAO          familyDAO = new FamilyDAOImpl();
    private final AidDistributionDAO distDAO   = new AidDistributionDAOImpl();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblWelcome.setText("Welcome, " + SessionManager.getCurrentUser().getFullName());
        String orgName = SessionManager.getCurrentUser().getOrganizationName();
        if (lblOrgName != null) lblOrgName.setText(orgName != null ? orgName : "");
        loadStats();
    }

    private void loadStats() {
        lblTotalFamilies.setText(String.valueOf(familyDAO.countAll()));
        lblUnservedFamilies.setText(String.valueOf(familyDAO.countUnserved()));
        int orgId = SessionManager.getCurrentUser().getOrgId();
        lblServedByOrg.setText(String.valueOf(distDAO.countServedByOrg(orgId)));
    }

    // ── Navigation ────────────────────────────────────────────────────────────
    @FXML private void onNavDashboard(ActionEvent e) {
        SceneManager.switchScene("/com/ghads/view/coordinator/CoordDashboard.fxml", "Coordinator Dashboard");
    }
    @FXML private void onNavFamilies(ActionEvent e) {
        SceneManager.switchScene("/com/ghads/view/coordinator/CoordFamilies.fxml", "Family Management");
    }
    @FXML private void onNavDistribution(ActionEvent e) {
        SceneManager.switchScene("/com/ghads/view/coordinator/CoordDistribution.fxml", "Aid Distribution");
    }
    @FXML private void onNavProfile(ActionEvent e) {
        SceneManager.switchScene("/com/ghads/view/coordinator/CoordProfile.fxml", "My Profile");
    }
    @FXML private void onNavChangePassword(ActionEvent e) {
        SceneManager.switchScene("/com/ghads/view/coordinator/CoordChangePassword.fxml", "Change Password");
    }
    @FXML private void onRefresh(ActionEvent e) { loadStats(); }
}
