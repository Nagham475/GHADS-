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
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller for CoordProfile.fxml — coordinator views/edits own profile and photo.
 */
public class CoordProfileController extends BaseController implements Initializable {

    @FXML private TextField txfFullName;
    @FXML private TextField txfUsername;
    @FXML private TextField txfEmail;
    @FXML private Label     lblOrg;
    @FXML private Label     lblRole;
    @FXML private ImageView imgPhoto;
    @FXML private Label     lblPhotoPath;

    private final UserDAO userDAO = new UserDAOImpl();
    private String newPhotoPath;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        loadProfile();
    }

    private void loadProfile() {
        User u = SessionManager.getCurrentUser();
        txfFullName.setText(u.getFullName());
        txfUsername.setText(u.getUsername());
        txfEmail.setText(u.getEmail());
        if (lblRole != null) lblRole.setText(u.getRole());
        if (lblOrg  != null) lblOrg.setText(
            u.getOrganizationName() != null ? u.getOrganizationName() : "N/A");

        if (u.getPhotoPath() != null && !u.getPhotoPath().isBlank()) {
            try {
                imgPhoto.setImage(new Image("file:" + u.getPhotoPath()));
                if (lblPhotoPath != null) lblPhotoPath.setText(new File(u.getPhotoPath()).getName());
            } catch (Exception ignored) {}
        }
    }

    @FXML
    private void onChoosePhoto(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Select Profile Photo");
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        File file = fc.showOpenDialog(SceneManager.getPrimaryStage());
        if (file != null) {
            newPhotoPath = file.getAbsolutePath();
            if (lblPhotoPath != null) lblPhotoPath.setText(file.getName());
            imgPhoto.setImage(new Image(file.toURI().toString()));
        }
    }

    @FXML
    private void onSave(ActionEvent event) {
        String fullName = txfFullName.getText().trim();
        String email    = txfEmail.getText().trim();

        if (fullName.isBlank()) { AlertHelper.showError("Validation", "Full name is required."); return; }
        if (email.isBlank())    { AlertHelper.showError("Validation", "Email is required.");     return; }
        if (!email.matches("^[\\w.+\\-]+@[\\w\\-]+(\\.[\\w\\-]+)+$")) {
            AlertHelper.showError("Validation", "Email address is not valid."); return;
        }

        User u = SessionManager.getCurrentUser();
        if (userDAO.existsByEmailExcluding(email, u.getUserId())) {
            AlertHelper.showError("Validation", "This email is already used by another account.");
            return;
        }

        u.setFullName(fullName);
        u.setEmail(email);
        if (newPhotoPath != null) {
            u.setPhotoPath(newPhotoPath);
            userDAO.updatePhoto(u.getUserId(), newPhotoPath);
        }
        userDAO.update(u);
        AlertHelper.showSuccess("Success", "Profile updated successfully.");
    }

    @FXML private void onBack(ActionEvent e) { onNavDashboard(e); }

    // ── Sidebar navigation ────────────────────────────────────────────────────
    @FXML private void onNavDashboard(ActionEvent e) {
        SceneManager.switchScene("/com/ghads/view/coordinator/CoordDashboard.fxml", "Coordinator Dashboard");
    }
    @FXML private void onNavFamilies(ActionEvent e) {
        SceneManager.switchScene("/com/ghads/view/coordinator/CoordFamilies.fxml", "Family Management");
    }
    @FXML private void onNavDistribution(ActionEvent e) {
        SceneManager.switchScene("/com/ghads/view/coordinator/CoordDistribution.fxml", "Aid Distribution");
    }
    @FXML private void onNavProfile(ActionEvent e) { /* already here */ }
    @FXML private void onNavChangePassword(ActionEvent e) {
        SceneManager.switchScene("/com/ghads/view/coordinator/CoordChangePassword.fxml", "Change Password");
    }
}
