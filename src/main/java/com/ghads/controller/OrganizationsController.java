package com.ghads.controller;

import com.ghads.dao.OrganizationDAO;
import com.ghads.dao.impl.OrganizationDAOImpl;
import com.ghads.model.Organization;
import com.ghads.util.AlertHelper;
import com.ghads.util.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller for Organizations.fxml — Admin CRUD for organizations.
 */
public class OrganizationsController extends BaseController implements Initializable {

    @FXML private TextField txfName;
    @FXML private TextField txfType;
    @FXML private TextField txfContact;

    @FXML private TableView<Organization>            tblOrgs;
    @FXML private TableColumn<Organization, Integer> colId;
    @FXML private TableColumn<Organization, String>  colName;
    @FXML private TableColumn<Organization, String>  colType;
    @FXML private TableColumn<Organization, String>  colContact;

    @FXML private Button btnSave;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Label  lblStatus;

    private final OrganizationDAO orgDAO = new OrganizationDAOImpl();
    private ObservableList<Organization> orgList;
    private Organization selectedOrg;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();
        loadOrganizations();
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);

        tblOrgs.getSelectionModel().selectedItemProperty().addListener((obs, old, newSel) -> {
            if (newSel != null) {
                selectedOrg = newSel;
                populateForm(newSel);
                btnUpdate.setDisable(false);
                btnDelete.setDisable(false);
                btnSave.setDisable(true);
            }
        });
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("orgId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colType.setCellValueFactory(new PropertyValueFactory<>("type"));
        colContact.setCellValueFactory(new PropertyValueFactory<>("contactInfo"));
    }

    private void loadOrganizations() {
        orgList = FXCollections.observableArrayList(orgDAO.findAll());
        tblOrgs.setItems(orgList);
        lblStatus.setText("Total: " + orgList.size() + " organizations");
    }

    @FXML
    private void onSave(ActionEvent event) {
        if (!validateForm()) return;
        String name = txfName.getText().trim();
        if (orgDAO.findByName(name).isPresent()) {
            AlertHelper.showError("Duplicate", "An organization with this name already exists.");
            return;
        }
        Organization org = new Organization(name, txfType.getText().trim(), txfContact.getText().trim());
        orgDAO.insert(org);
        AlertHelper.showSuccess("Success", "Organization added successfully.");
        clearForm();
        loadOrganizations();
    }

    @FXML
    private void onUpdate(ActionEvent event) {
        if (selectedOrg == null) { AlertHelper.showWarning("No Selection", "Select an organization to update."); return; }
        if (!validateForm()) return;
        String name = txfName.getText().trim();
        Optional<Organization> existing = orgDAO.findByName(name);
        if (existing.isPresent() && existing.get().getOrgId() != selectedOrg.getOrgId()) {
            AlertHelper.showError("Duplicate", "Another organization with this name already exists.");
            return;
        }
        selectedOrg.setName(name);
        selectedOrg.setType(txfType.getText().trim());
        selectedOrg.setContactInfo(txfContact.getText().trim());
        orgDAO.update(selectedOrg);
        AlertHelper.showSuccess("Success", "Organization updated successfully.");
        clearForm();
        loadOrganizations();
    }

    @FXML
    private void onDelete(ActionEvent event) {
        if (selectedOrg == null) { AlertHelper.showWarning("No Selection", "Select an organization to delete."); return; }
        if (!AlertHelper.showConfirmation("Confirm Delete",
                "Delete organization: " + selectedOrg.getName() + "?\nThis will remove all linked users and distributions."))
            return;
        orgDAO.delete(selectedOrg.getOrgId());
        AlertHelper.showSuccess("Success", "Organization deleted.");
        clearForm();
        loadOrganizations();
    }

    @FXML private void onClear(ActionEvent e)   { clearForm(); }
    @FXML private void onRefresh(ActionEvent e) { loadOrganizations(); }

    // ── Sidebar navigation ────────────────────────────────────────────────────
    @FXML private void onNavDashboard(ActionEvent e) {
        SceneManager.switchScene("/com/ghads/view/admin/AdminDashboard.fxml", "Admin Dashboard");
    }
    @FXML private void onNavOrganizations(ActionEvent e) { /* already here */ }
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

    // ── Helpers ───────────────────────────────────────────────────────────────
    private boolean validateForm() {
        if (txfName.getText().isBlank())    { AlertHelper.showError("Validation", "Name is required.");         return false; }
        if (txfType.getText().isBlank())    { AlertHelper.showError("Validation", "Type is required.");         return false; }
        if (txfContact.getText().isBlank()) { AlertHelper.showError("Validation", "Contact info is required."); return false; }
        return true;
    }

    private void populateForm(Organization org) {
        txfName.setText(org.getName());
        txfType.setText(org.getType());
        txfContact.setText(org.getContactInfo());
    }

    private void clearForm() {
        txfName.clear(); txfType.clear(); txfContact.clear();
        selectedOrg = null;
        tblOrgs.getSelectionModel().clearSelection();
        btnSave.setDisable(false);
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
        lblStatus.setText("");
    }
}
