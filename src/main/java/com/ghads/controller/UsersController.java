package com.ghads.controller;

import com.ghads.dao.OrganizationDAO;
import com.ghads.dao.UserDAO;
import com.ghads.dao.impl.OrganizationDAOImpl;
import com.ghads.dao.impl.UserDAOImpl;
import com.ghads.model.Organization;
import com.ghads.model.User;
import com.ghads.util.AlertHelper;
import com.ghads.util.SceneManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Controller for Users.fxml — Admin manages coordinator accounts with photo upload.
 */
public class UsersController extends BaseController implements Initializable {

    @FXML private TextField     txfFullName;
    @FXML private TextField     txfUsername;
    @FXML private PasswordField txfPassword;
    @FXML private TextField     txfEmail;
    @FXML private ComboBox<String>       cmbRole;
    @FXML private ComboBox<Organization> cmbOrg;
    @FXML private ImageView              imgPhoto;
    @FXML private Label                  lblPhotoPath;

    @FXML private TableView<User>            tblUsers;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String>  colFullName;
    @FXML private TableColumn<User, String>  colUsername;
    @FXML private TableColumn<User, String>  colEmail;
    @FXML private TableColumn<User, String>  colRole;
    @FXML private TableColumn<User, String>  colOrg;

    @FXML private Button btnSave;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;

    private final UserDAO         userDAO = new UserDAOImpl();
    private final OrganizationDAO orgDAO  = new OrganizationDAOImpl();
    private User    selectedUser;
    private String  selectedPhotoPath;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        cmbRole.setItems(FXCollections.observableArrayList("COORDINATOR", "ADMIN"));
        cmbRole.setValue("COORDINATOR");
        loadOrganizations();
        setupTable();
        loadUsers();
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);

        tblUsers.getSelectionModel().selectedItemProperty().addListener((obs, old, newSel) -> {
            if (newSel != null) {
                selectedUser = newSel;
                populateForm(newSel);
                btnSave.setDisable(true);
                btnUpdate.setDisable(false);
                btnDelete.setDisable(false);
            }
        });

        cmbRole.valueProperty().addListener((obs, old, newVal) ->
            cmbOrg.setDisable("ADMIN".equals(newVal))
        );
    }

    private void setupTable() {
        colId.setCellValueFactory(new PropertyValueFactory<>("userId"));
        colFullName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colOrg.setCellValueFactory(new PropertyValueFactory<>("organizationName"));
    }

    private void loadUsers() {
        tblUsers.setItems(FXCollections.observableArrayList(userDAO.findAll()));
    }

    private void loadOrganizations() {
        List<Organization> orgs = orgDAO.findAll();
        cmbOrg.setItems(FXCollections.observableArrayList(orgs));
        cmbOrg.setConverter(new StringConverter<>() {
            @Override public String toString(Organization o)    { return o == null ? "" : o.getName(); }
            @Override public Organization fromString(String s)  { return null; }
        });
    }

    @FXML
    private void onChoosePhoto(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Profile Photo");
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File file = fc.showOpenDialog(SceneManager.getPrimaryStage());
        if (file != null) {
            selectedPhotoPath = file.getAbsolutePath();
            lblPhotoPath.setText(file.getName());
            imgPhoto.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    private void onSave(ActionEvent event) {
        if (!validateForm(true)) return;
        User user = new User();
        populateUserFromForm(user);
        user.setPhotoPath(selectedPhotoPath);
        userDAO.insert(user);
        AlertHelper.showSuccess("Success", "User created successfully.");
        clearForm();
        loadUsers();
    }

    @FXML
    private void onUpdate(ActionEvent event) {
        if (selectedUser == null) { AlertHelper.showWarning("No Selection", "Select a user to update."); return; }
        if (!validateForm(false)) return;
        populateUserFromForm(selectedUser);
        if (selectedPhotoPath != null) selectedUser.setPhotoPath(selectedPhotoPath);
        userDAO.update(selectedUser);
        AlertHelper.showSuccess("Success", "User updated successfully.");
        clearForm();
        loadUsers();
    }

    @FXML
    private void onDelete(ActionEvent event) {
        if (selectedUser == null) { AlertHelper.showWarning("No Selection", "Select a user to delete."); return; }
        if (!AlertHelper.showConfirmation("Confirm Delete", "Delete user: " + selectedUser.getFullName() + "?")) return;
        userDAO.delete(selectedUser.getUserId());
        AlertHelper.showSuccess("Success", "User deleted.");
        clearForm();
        loadUsers();
    }

    @FXML private void onClear(ActionEvent e)   { clearForm(); }
    @FXML private void onRefresh(ActionEvent e) { loadUsers(); }

    // ── Sidebar navigation ────────────────────────────────────────────────────
    @FXML private void onNavDashboard(ActionEvent e) {
        SceneManager.switchScene("/com/ghads/view/admin/AdminDashboard.fxml", "Admin Dashboard");
    }
    @FXML private void onNavOrganizations(ActionEvent e) {
        SceneManager.switchScene("/com/ghads/view/admin/Organizations.fxml", "Organizations");
    }
    @FXML private void onNavUsers(ActionEvent e) { /* already here */ }
    @FXML private void onNavFamilies(ActionEvent e) {
        SceneManager.switchScene("/com/ghads/view/admin/Families.fxml", "Families");
    }
    @FXML private void onNavDistributions(ActionEvent e) {
        SceneManager.switchScene("/com/ghads/view/admin/Distributions.fxml", "Aid Distributions");
    }
    @FXML private void onNavChangePassword(ActionEvent e) {
        SceneManager.switchScene("/com/ghads/view/admin/ChangePassword.fxml", "Change Password");
    }

    // ── Validation ────────────────────────────────────────────────────────────
    private boolean validateForm(boolean isInsert) {
        if (txfFullName.getText().isBlank()) { err("Full name is required.");          return false; }
        if (txfUsername.getText().isBlank()) { err("Username is required.");           return false; }
        if (isInsert && txfPassword.getText().length() < 8) {
            err("Password must be at least 8 characters."); return false;
        }
        if (txfEmail.getText().isBlank())    { err("Email is required.");              return false; }
        if (cmbRole.getValue() == null)      { err("Role is required.");               return false; }
        if ("COORDINATOR".equals(cmbRole.getValue()) && cmbOrg.getValue() == null) {
            err("Organization is required for Coordinator."); return false;
        }
        int excludeId = isInsert ? 0 : selectedUser.getUserId();
        if (isInsert ? userDAO.existsByUsername(txfUsername.getText().trim())
                     : userDAO.existsByUsernameExcluding(txfUsername.getText().trim(), excludeId)) {
            err("Username already exists."); return false;
        }
        if (isInsert ? userDAO.existsByEmail(txfEmail.getText().trim())
                     : userDAO.existsByEmailExcluding(txfEmail.getText().trim(), excludeId)) {
            err("Email already exists."); return false;
        }
        return true;
    }

    private void err(String msg) { AlertHelper.showError("Validation", msg); }

    private void populateUserFromForm(User user) {
        user.setFullName(txfFullName.getText().trim());
        user.setUsername(txfUsername.getText().trim());
        if (!txfPassword.getText().isBlank()) user.setPassword(txfPassword.getText());
        user.setEmail(txfEmail.getText().trim());
        user.setRole(cmbRole.getValue());
        Organization org = cmbOrg.getValue();
        user.setOrgId(org != null ? org.getOrgId() : 0);
    }

    private void populateForm(User user) {
        txfFullName.setText(user.getFullName());
        txfUsername.setText(user.getUsername());
        txfPassword.clear();
        txfEmail.setText(user.getEmail());
        cmbRole.setValue(user.getRole());
        cmbOrg.getItems().stream()
            .filter(o -> o.getOrgId() == user.getOrgId())
            .findFirst().ifPresent(cmbOrg::setValue);
        if (user.getPhotoPath() != null && !user.getPhotoPath().isBlank()) {
            try {
                imgPhoto.setImage(new Image("file:" + user.getPhotoPath()));
                lblPhotoPath.setText(new File(user.getPhotoPath()).getName());
            } catch (Exception ignored) {}
        }
    }

    private void clearForm() {
        txfFullName.clear(); txfUsername.clear(); txfPassword.clear();
        txfEmail.clear(); cmbRole.setValue("COORDINATOR"); cmbOrg.setValue(null);
        if (imgPhoto != null) imgPhoto.setImage(null);
        if (lblPhotoPath != null) lblPhotoPath.setText("No photo selected");
        selectedUser = null; selectedPhotoPath = null;
        tblUsers.getSelectionModel().clearSelection();
        btnSave.setDisable(false); btnUpdate.setDisable(true); btnDelete.setDisable(true);
    }
}
