package com.ghads.controller;

import com.ghads.dao.*;
import com.ghads.dao.impl.*;
import com.ghads.model.AidDistribution;
import com.ghads.util.SceneManager;
import com.ghads.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for AdminDashboard.fxml
 */
public class AdminDashboardController extends BaseController implements Initializable {

    @FXML private Label lblTotalOrgs;
    @FXML private Label lblTotalUsers;
    @FXML private Label lblTotalFamilies;
    @FXML private Label lblServedFamilies;
    @FXML private Label lblUnservedFamilies;
    @FXML private Label lblWelcome;

    @FXML private TableView<AidDistribution>            tblRecent;
    @FXML private TableColumn<AidDistribution, String>  colFamily;
    @FXML private TableColumn<AidDistribution, String>  colOrg;
    @FXML private TableColumn<AidDistribution, String>  colAidType;
    @FXML private TableColumn<AidDistribution, String>  colDate;
    @FXML private TableColumn<AidDistribution, String>  colVuln;

    private final OrganizationDAO    orgDAO    = new OrganizationDAOImpl();
    private final UserDAO            userDAO   = new UserDAOImpl();
    private final FamilyDAO          familyDAO = new FamilyDAOImpl();
    private final AidDistributionDAO distDAO   = new AidDistributionDAOImpl();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lblWelcome.setText("Welcome, " + SessionManager.getCurrentUser().getFullName());
        setupTable();
        loadStats();
        loadRecentDistributions();
    }

    private void setupTable() {
        colFamily.setCellValueFactory(new PropertyValueFactory<>("familyName"));
        colOrg.setCellValueFactory(new PropertyValueFactory<>("organizationName"));
        colAidType.setCellValueFactory(new PropertyValueFactory<>("aidType"));
        colDate.setCellValueFactory(new PropertyValueFactory<>("distributionDate"));
        colVuln.setCellValueFactory(new PropertyValueFactory<>("vulnerabilityLevel"));
    }

    private void loadStats() {
        lblTotalOrgs.setText(String.valueOf(orgDAO.count()));
        lblTotalUsers.setText(String.valueOf(userDAO.countCoordinators()));
        lblTotalFamilies.setText(String.valueOf(familyDAO.countAll()));
        lblServedFamilies.setText(String.valueOf(familyDAO.countServed()));
        lblUnservedFamilies.setText(String.valueOf(familyDAO.countUnserved()));
    }

    private void loadRecentDistributions() {
        List<AidDistribution> recent = distDAO.findAll().stream()
            .sorted((a, b) -> b.getDistributionDate().compareTo(a.getDistributionDate()))
            .limit(10)
            .collect(Collectors.toList());
        tblRecent.setItems(FXCollections.observableArrayList(recent));
    }

    // ── Navigation ────────────────────────────────────────────────────────────

    @FXML private void onNavDashboard(ActionEvent e) {
        SceneManager.switchScene("/com/ghads/view/admin/AdminDashboard.fxml", "Admin Dashboard");
    }
    @FXML private void onNavOrganizations(ActionEvent e) {
        SceneManager.switchScene("/com/ghads/view/admin/Organizations.fxml", "Organizations");
    }
    @FXML private void onNavUsers(ActionEvent e) {
        SceneManager.switchScene("/com/ghads/view/admin/Users.fxml", "Users");
    }
    @FXML private void onNavFamilies(ActionEvent e) {
        SceneManager.switchScene("/com/ghads/view/admin/Families.fxml", "Families");
    }
    @FXML private void onNavDistributions(ActionEvent e) {
        SceneManager.switchScene("/com/ghads/view/admin/Distributions.fxml", "Aid Distributions");
    }
    @FXML private void onNavChangePassword(ActionEvent e) {
        SceneManager.switchScene("/com/ghads/view/admin/ChangePassword.fxml", "Change Password");
    }
    @FXML private void onRefresh(ActionEvent e) {
        loadStats();
        loadRecentDistributions();
    }
}
