package com.ghads.controller;

import com.ghads.dao.AidDistributionDAO;
import com.ghads.dao.FamilyDAO;
import com.ghads.dao.OrganizationDAO;
import com.ghads.dao.impl.*;
import com.ghads.model.AidDistribution;
import com.ghads.model.Family;
import com.ghads.model.Organization;
import com.ghads.service.AidDistributionService;
import com.ghads.util.AlertHelper;
import com.ghads.util.SceneManager;
import com.ghads.util.SessionManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for admin/Distributions.fxml AND coordinator/CoordDistribution.fxml.
 * Duplicate check logic is delegated to AidDistributionService.
 */
public class DistributionsController extends BaseController implements Initializable {

    // ── Entry form (coordinator only) 
    @FXML private ComboBox<Family>  cmbFamily;
    @FXML private ComboBox<String>  cmbAidType;
    @FXML private DatePicker        dpDistDate;
    @FXML private TextArea          txaNote;
    @FXML private Label             lblFamilyInfo;

    // ── Filter row 
    @FXML private ComboBox<Organization> cmbFilterOrg;
    @FXML private Button                 btnSearchByOrg;
    @FXML private Button                 btnShowAll;

    // ── Table 
    @FXML private TableView<AidDistribution>            tblDist;
    @FXML private TableColumn<AidDistribution, Integer> colId;
    @FXML private TableColumn<AidDistribution, String>  colFamily;
    @FXML private TableColumn<AidDistribution, String>  colOrg;
    @FXML private TableColumn<AidDistribution, String>  colCoord;
    @FXML private TableColumn<AidDistribution, String>  colDate;
    @FXML private TableColumn<AidDistribution, String>  colAidType;
    @FXML private TableColumn<AidDistribution, String>  colVuln;
    @FXML private Label                                  lblCount;

    // ── Coordinator-only buttons 
    @FXML private Button btnSave;
    @FXML private Button btnFilterHigh;
    @FXML private Button btnFilterUnserved;

    private final AidDistributionDAO    distDAO     = new AidDistributionDAOImpl();
    private final FamilyDAO             familyDAO   = new FamilyDAOImpl();
    private final OrganizationDAO       orgDAO      = new OrganizationDAOImpl();
    private final AidDistributionService distService = new AidDistributionService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupTable();

        if (cmbAidType != null)
            cmbAidType.setItems(FXCollections.observableArrayList(
                "Food", "Medicine", "Clothes", "Hygiene Kit",
                "School Supplies", "Cash Voucher", "Other"));

        if (dpDistDate != null) dpDistDate.setValue(LocalDate.now());

        loadOrganizationFilter();

        if (SessionManager.isAdmin()) {
            hideEntryForm();
            loadAllDistributions();
        } else {
            loadFamiliesForCoord();
            loadCoordDistributions();
        }

        if (cmbFamily != null) {
            cmbFamily.valueProperty().addListener((obs, old, sel) -> {
                if (sel != null && lblFamilyInfo != null)
                    lblFamilyInfo.setText("Vulnerability: " + sel.getVulnerabilityLevel() +
                        "  |  Last Aid: " +
                        (sel.getLastAidDate() != null ? sel.getLastAidDate() : "Never served"));
            });
        }
    }

    // ── Table setup 
    private void setupTable() {
        if (colId      != null) colId.setCellValueFactory(new PropertyValueFactory<>("distributionId"));
        if (colFamily  != null) colFamily.setCellValueFactory(new PropertyValueFactory<>("familyName"));
        if (colOrg     != null) colOrg.setCellValueFactory(new PropertyValueFactory<>("organizationName"));
        if (colCoord   != null) colCoord.setCellValueFactory(new PropertyValueFactory<>("coordinatorName"));
        if (colDate    != null) colDate.setCellValueFactory(new PropertyValueFactory<>("distributionDate"));
        if (colAidType != null) colAidType.setCellValueFactory(new PropertyValueFactory<>("aidType"));
        if (colVuln    != null) colVuln.setCellValueFactory(new PropertyValueFactory<>("vulnerabilityLevel"));
    }

    private void loadOrganizationFilter() {
        if (cmbFilterOrg == null) return;
        cmbFilterOrg.setItems(FXCollections.observableArrayList(orgDAO.findAll()));
        cmbFilterOrg.setConverter(new StringConverter<>() {
            @Override public String toString(Organization o) { return o == null ? "All" : o.getName(); }
            @Override public Organization fromString(String s) { return null; }
        });
    }

    private void loadFamiliesForCoord() {
        if (cmbFamily == null) return;
        cmbFamily.setItems(FXCollections.observableArrayList(familyDAO.findHighVulnerabilityFirst()));
        cmbFamily.setConverter(new StringConverter<>() {
            @Override public String toString(Family f) {
                return f == null ? "" : f.getHouseholdName() + "  [" + f.getVulnerabilityLevel() + "]";
            }
            @Override public Family fromString(String s) { return null; }
        });
    }

    private void loadAllDistributions() {
        List<AidDistribution> list = distDAO.findAll();
        tblDist.setItems(FXCollections.observableArrayList(list));
        updateCount(list.size());
    }

    private void loadCoordDistributions() {
        int orgId = SessionManager.getCurrentUser().getOrgId();
        List<AidDistribution> list = distDAO.findByOrgId(orgId);
        tblDist.setItems(FXCollections.observableArrayList(list));
        updateCount(list.size());
    }

    // ── Save distribution (Coordinator) ──────────────────────────────────────
    @FXML
    private void onSave(ActionEvent event) {
        if (!validateEntry()) return;

        Family    family  = cmbFamily.getValue();
        int       orgId   = SessionManager.getCurrentUser().getOrgId();
        int       userId  = SessionManager.getCurrentUser().getUserId();
        String    aidType = cmbAidType.getValue();
        LocalDate date    = dpDistDate.getValue();
        String    notes   = txaNote != null ? txaNote.getText() : "";

        AidDistribution dist = new AidDistribution(
            family.getFamilyId(), orgId, userId, date, aidType, notes);

        AidDistributionService.DuplicateCheckResult result = distService.saveDistribution(dist);

        if (result.isAllowed()) {
            AlertHelper.showSuccess("Distribution Recorded",
                "Aid distributed successfully to " + family.getHouseholdName() + ".");
            clearEntryForm();
            loadCoordDistributions();
        } else {
            AlertHelper.showWarning("Distribution Rejected — Duplicate Detected",
                AidDistributionService.buildRejectionMessage(result.getConflictingRecord()));
        }
    }

    // ── Filter / search ───────────────────────────────────────────────────────
    @FXML
    private void onSearchByOrg(ActionEvent event) {
        if (cmbFilterOrg == null || cmbFilterOrg.getValue() == null) {
            AlertHelper.showWarning("No Organization", "Please select an organization to filter by.");
            return;
        }
        List<AidDistribution> list = distDAO.findByOrgId(cmbFilterOrg.getValue().getOrgId());
        tblDist.setItems(FXCollections.observableArrayList(list));
        updateCount(list.size());
    }

    @FXML
    private void onShowAll(ActionEvent event) {
        if (SessionManager.isAdmin()) loadAllDistributions();
        else loadCoordDistributions();
    }

    @FXML
    private void onFilterHighVuln(ActionEvent event) {
        List<AidDistribution> source = SessionManager.isAdmin()
            ? distDAO.findAll()
            : distDAO.findByOrgId(SessionManager.getCurrentUser().getOrgId());
        // Streams + Lambda — required by spec
        var filtered = source.stream()
            .filter(d -> "HIGH".equals(d.getVulnerabilityLevel()))
            .toList();
        tblDist.setItems(FXCollections.observableArrayList(filtered));
        updateCount(filtered.size());
    }

    @FXML
    private void onFilterUnserved(ActionEvent event) {
        // Navigate to families screen filtered to unserved
        SceneManager.switchScene(
            SessionManager.isAdmin()
                ? "/com/ghads/view/admin/Families.fxml"
                : "/com/ghads/view/coordinator/CoordFamilies.fxml",
            "Unserved Families");
    }

    @FXML private void onClear(ActionEvent e)   { clearEntryForm(); }
    @FXML private void onRefresh(ActionEvent e) {
        if (SessionManager.isAdmin()) loadAllDistributions();
        else loadCoordDistributions();
    }

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
    @FXML private void onNavFamilies(ActionEvent e) {
        if (SessionManager.isAdmin())
            SceneManager.switchScene("/com/ghads/view/admin/Families.fxml", "Families");
        else
            SceneManager.switchScene("/com/ghads/view/coordinator/CoordFamilies.fxml", "Families");
    }
    @FXML private void onNavDistributions(ActionEvent e) { /* already here - admin */ }
    @FXML private void onNavDistribution(ActionEvent e)  { /* already here - coord */ }
    @FXML private void onNavProfile(ActionEvent e) {
        SceneManager.switchScene("/com/ghads/view/coordinator/CoordProfile.fxml", "My Profile");
    }
    @FXML private void onNavChangePassword(ActionEvent e) {
        if (SessionManager.isAdmin())
            SceneManager.switchScene("/com/ghads/view/admin/ChangePassword.fxml", "Change Password");
        else
            SceneManager.switchScene("/com/ghads/view/coordinator/CoordChangePassword.fxml", "Change Password");
    }
    @FXML private void onBack(ActionEvent e) { onNavDashboard(e); }

    // ── Helpers ───────────────────────────────────────────────────────────────
    private boolean validateEntry() {
        if (cmbFamily  == null || cmbFamily.getValue()  == null) {
            AlertHelper.showError("Validation", "Please select a family."); return false; }
        if (cmbAidType == null || cmbAidType.getValue() == null || cmbAidType.getValue().isBlank()) {
            AlertHelper.showError("Validation", "Please select an aid type."); return false; }
        if (dpDistDate == null || dpDistDate.getValue() == null) {
            AlertHelper.showError("Validation", "Please select a distribution date."); return false; }
        return true;
    }

    private void clearEntryForm() {
        if (cmbFamily   != null) cmbFamily.setValue(null);
        if (cmbAidType  != null) cmbAidType.setValue("Food");
        if (dpDistDate  != null) dpDistDate.setValue(LocalDate.now());
        if (txaNote     != null) txaNote.clear();
        if (lblFamilyInfo != null) lblFamilyInfo.setText("");
    }

    private void hideEntryForm() {
        if (btnSave           != null) { btnSave.setVisible(false); btnSave.setManaged(false); }
        if (btnFilterHigh     != null) { btnFilterHigh.setVisible(false); btnFilterHigh.setManaged(false); }
        if (btnFilterUnserved != null) { btnFilterUnserved.setVisible(false); btnFilterUnserved.setManaged(false); }
        if (cmbFamily         != null) { cmbFamily.setVisible(false); cmbFamily.setManaged(false); }
        if (dpDistDate        != null) { dpDistDate.setVisible(false); dpDistDate.setManaged(false); }
        if (txaNote           != null) { txaNote.setVisible(false); txaNote.setManaged(false); }
        if (lblFamilyInfo     != null) { lblFamilyInfo.setVisible(false); lblFamilyInfo.setManaged(false); }
    }

    private void updateCount(int n) {
        if (lblCount != null) lblCount.setText("Records shown: " + n);
    }
}
