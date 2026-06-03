package com.ghads.controller;

import com.ghads.dao.FamilyDAO;
import com.ghads.dao.impl.FamilyDAOImpl;
import com.ghads.model.Family;
import com.ghads.util.AlertHelper;
import com.ghads.util.SceneManager;
import com.ghads.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * Controller for Families.fxml and CoordFamilies.fxml.
 * Role-aware: hides Delete for Coordinators, adjusts navigation.
 */
public class FamiliesController extends BaseController implements Initializable {

    @FXML private TextField        txfHouseholdName;
    @FXML private TextField        txfPhone;
    @FXML private TextField        txfLocation;
    @FXML private TextField        txfFamilySize;
    @FXML private TextField        txfNationalId;
    @FXML private ComboBox<String> cmbVulnerability;
    @FXML private DatePicker       dpRegistrationDate;

    @FXML private TableView<Family>            tblFamilies;
    @FXML private TableColumn<Family, Integer> colId;
    @FXML private TableColumn<Family, String>  colName;
    @FXML private TableColumn<Family, String>  colNationalId;
    @FXML private TableColumn<Family, String>  colLocation;
    @FXML private TableColumn<Family, Integer> colSize;
    @FXML private TableColumn<Family, String>  colVuln;
    @FXML private TableColumn<Family, String>  colLastAid;
    @FXML private TableColumn<Family, String>  colStatus;

    @FXML private Button btnSave;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Label  lblCount;

    private final FamilyDAO familyDAO = new FamilyDAOImpl();
    private ObservableList<Family> familyList;
    private Family selectedFamily;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbVulnerability.setItems(FXCollections.observableArrayList("HIGH", "MEDIUM", "LOW"));
        dpRegistrationDate.setValue(LocalDate.now());
        setupTable();
        loadFamilies();
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);

        if (SessionManager.isCoordinator() && btnDelete != null) {
            btnDelete.setVisible(false);
            btnDelete.setManaged(false);
        }

        tblFamilies.getSelectionModel().selectedItemProperty().addListener((obs, old, newSel) -> {
            if (newSel != null) {
                selectedFamily = newSel;
                populateForm(newSel);
                btnSave.setDisable(true);
                btnUpdate.setDisable(false);
                if (btnDelete != null && SessionManager.isAdmin()) btnDelete.setDisable(false);
            }
        });
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("familyId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("householdName"));
        colNationalId.setCellValueFactory(new PropertyValueFactory<>("nationalId"));
        colLocation.setCellValueFactory(new PropertyValueFactory<>("location"));
        colSize.setCellValueFactory(new PropertyValueFactory<>("familySize"));
        colVuln.setCellValueFactory(new PropertyValueFactory<>("vulnerabilityLevel"));
        colLastAid.setCellValueFactory(new PropertyValueFactory<>("lastAidDate"));

        // Color-code vulnerability column
        colVuln.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String v, boolean empty) {
                super.updateItem(v, empty);
                setText(empty ? null : v);
                getStyleClass().removeAll("vuln-high", "vuln-medium", "vuln-low");
                if (!empty && v != null) {
                    switch (v) {
                        case "HIGH"   -> getStyleClass().add("vuln-high");
                        case "MEDIUM" -> getStyleClass().add("vuln-medium");
                        case "LOW"    -> getStyleClass().add("vuln-low");
                    }
                }
            }
        });

        // Computed Served/Unserved status column
        if (colStatus != null) {
            colStatus.setCellFactory(col -> new TableCell<>() {
                @Override protected void updateItem(String v, boolean empty) {
                    super.updateItem(v, empty);
                    if (empty || getIndex() >= getTableView().getItems().size()) { setText(null); return; }
                    Family f = getTableView().getItems().get(getIndex());
                    setText(f.isServed() ? "✔ Served" : "✘ Unserved");
                    setStyle(f.isServed() ? "-fx-text-fill:#16a34a;-fx-font-weight:bold;"
                                          : "-fx-text-fill:#dc2626;-fx-font-weight:bold;");
                }
            });
        }
    }

    private void loadFamilies() {
        List<Family> families = familyDAO.findHighVulnerabilityFirst();
        familyList = FXCollections.observableArrayList(families);
        tblFamilies.setItems(familyList);
        updateCount();
    }

    @FXML
    private void onSave(ActionEvent event) {
        if (!validateForm()) return;
        String nid = txfNationalId.getText().trim();
        if (familyDAO.existsByNationalId(nid)) {
            AlertHelper.showError("Duplicate National ID",
                "A family with National ID '" + nid + "' already exists in the system.");
            return;
        }
        Family family = new Family(
            txfHouseholdName.getText().trim(), txfPhone.getText().trim(),
            txfLocation.getText().trim(), Integer.parseInt(txfFamilySize.getText().trim()),
            nid, cmbVulnerability.getValue(), dpRegistrationDate.getValue());
        familyDAO.insert(family);
        AlertHelper.showSuccess("Success", "Family registered successfully.");
        clearForm(); loadFamilies();
    }

    @FXML
    private void onUpdate(ActionEvent event) {
        if (selectedFamily == null) { AlertHelper.showWarning("No Selection", "Select a family first."); return; }
        if (!validateForm()) return;
        String nid = txfNationalId.getText().trim();
        if (familyDAO.existsByNationalIdExcluding(nid, selectedFamily.getFamilyId())) {
            AlertHelper.showError("Duplicate National ID", "Another family with this National ID already exists.");
            return;
        }
        selectedFamily.setHouseholdName(txfHouseholdName.getText().trim());
        selectedFamily.setPhone(txfPhone.getText().trim());
        selectedFamily.setLocation(txfLocation.getText().trim());
        selectedFamily.setFamilySize(Integer.parseInt(txfFamilySize.getText().trim()));
        selectedFamily.setNationalId(nid);
        selectedFamily.setVulnerabilityLevel(cmbVulnerability.getValue());
        selectedFamily.setRegistrationDate(dpRegistrationDate.getValue());
        familyDAO.update(selectedFamily);
        AlertHelper.showSuccess("Success", "Family updated successfully.");
        clearForm(); loadFamilies();
    }

    @FXML
    private void onDelete(ActionEvent event) {
        if (selectedFamily == null) { AlertHelper.showWarning("No Selection", "Select a family first."); return; }
        if (!AlertHelper.showConfirmation("Confirm Delete",
                "Permanently delete family: " + selectedFamily.getHouseholdName() + "?")) return;
        familyDAO.delete(selectedFamily.getFamilyId());
        AlertHelper.showSuccess("Success", "Family deleted.");
        clearForm(); loadFamilies();
    }

    @FXML private void onFilterHighVuln(ActionEvent e) {
        List<Family> filtered = familyDAO.findHighVulnerabilityFirst().stream()
            .filter(f -> "HIGH".equals(f.getVulnerabilityLevel())).collect(Collectors.toList());
        tblFamilies.setItems(FXCollections.observableArrayList(filtered));
    }
    @FXML private void onFilterUnserved(ActionEvent e) {
        tblFamilies.setItems(FXCollections.observableArrayList(familyDAO.findUnserved()));
    }
    @FXML private void onFilterAll(ActionEvent e) { loadFamilies(); }
    @FXML private void onClear(ActionEvent e)     { clearForm(); }
    @FXML private void onRefresh(ActionEvent e)   { loadFamilies(); }

    // ── Sidebar navigation — role-aware ───────────────────────────────────────
    @FXML private void onNavDashboard(ActionEvent e) {
        if (SessionManager.isAdmin())
            SceneManager.switchScene("/com/ghads/view/admin/AdminDashboard.fxml", "Admin Dashboard");
        else
            SceneManager.switchScene("/com/ghads/view/coordinator/CoordDashboard.fxml", "Coordinator Dashboard");
    }
    @FXML private void onNavOrganizations(ActionEvent e) {
        SceneManager.switchScene("/com/ghads/view/admin/Organizations.fxml", "Organizations");
    }
    @FXML private void onNavUsers(ActionEvent e) {
        SceneManager.switchScene("/com/ghads/view/admin/Users.fxml", "Users");
    }
    @FXML private void onNavFamilies(ActionEvent e) { /* already here */ }
    @FXML private void onNavDistributions(ActionEvent e) {
        SceneManager.switchScene("/com/ghads/view/admin/Distributions.fxml", "Aid Distributions");
    }
    @FXML private void onNavDistribution(ActionEvent e) {
        SceneManager.switchScene("/com/ghads/view/coordinator/CoordDistribution.fxml", "Aid Distribution");
    }
    @FXML private void onNavProfile(ActionEvent e) {
        SceneManager.switchScene("/com/ghads/view/coordinator/CoordProfile.fxml", "My Profile");
    }
    @FXML private void onNavChangePassword(ActionEvent e) {
        if (SessionManager.isAdmin())
            SceneManager.switchScene("/com/ghads/view/admin/ChangePassword.fxml", "Change Password");
        else
            SceneManager.switchScene("/com/ghads/view/coordinator/CoordChangePassword.fxml", "Change Password");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private boolean validateForm() {
        if (txfHouseholdName.getText().isBlank()) { err("Household name is required.");    return false; }
        if (txfPhone.getText().isBlank())         { err("Phone is required.");             return false; }
        if (txfLocation.getText().isBlank())      { err("Location is required.");          return false; }
        if (txfNationalId.getText().isBlank())    { err("National ID is required.");       return false; }
        if (cmbVulnerability.getValue() == null)  { err("Vulnerability level is required."); return false; }
        if (dpRegistrationDate.getValue() == null){ err("Registration date is required."); return false; }
        try {
            int size = Integer.parseInt(txfFamilySize.getText().trim());
            if (size <= 0) { err("Family size must be a positive number."); return false; }
        } catch (NumberFormatException ex) { err("Family size must be a valid number."); return false; }
        return true;
    }
    private void err(String msg) { AlertHelper.showError("Validation", msg); }

    private void populateForm(Family f) {
        txfHouseholdName.setText(f.getHouseholdName());
        txfPhone.setText(f.getPhone());
        txfLocation.setText(f.getLocation());
        txfFamilySize.setText(String.valueOf(f.getFamilySize()));
        txfNationalId.setText(f.getNationalId());
        cmbVulnerability.setValue(f.getVulnerabilityLevel());
        dpRegistrationDate.setValue(f.getRegistrationDate());
    }

    private void clearForm() {
        txfHouseholdName.clear(); txfPhone.clear(); txfLocation.clear();
        txfFamilySize.clear(); txfNationalId.clear();
        cmbVulnerability.setValue(null);
        dpRegistrationDate.setValue(LocalDate.now());
        selectedFamily = null;
        tblFamilies.getSelectionModel().clearSelection();
        btnSave.setDisable(false); btnUpdate.setDisable(true);
        if (btnDelete != null) btnDelete.setDisable(true);
    }

    private void updateCount() {
        if (lblCount != null)
            lblCount.setText("Total: " + familyDAO.countAll() +
                "  |  Served: " + familyDAO.countServed() +
                "  |  Unserved: " + familyDAO.countUnserved());
    }
}
