package com.cinema.controller;

import com.cinema.model.User;
import com.cinema.model.UserRole;
import com.cinema.service.CinemaService;
import com.cinema.util.SceneManager;
import com.cinema.util.ValidationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

/**
 * Controller for the Admin Add User screen.
 */
public class AdminAddUserController {

    @FXML
    private TextField emailField;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField phoneField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField passwordTextField;

    @FXML
    private Button togglePasswordBtn;

    @FXML
    private PasswordField repeatPasswordField;

    @FXML
    private TextField repeatPasswordTextField;

    @FXML
    private Button toggleRepeatPasswordBtn;

    @FXML
    private ToggleButton adminToggle;

    @FXML
    private ToggleButton cashierToggle;

    @FXML
    private ToggleButton userToggle;

    @FXML
    private Label errorLabel;

    private final CinemaService cinemaService = CinemaService.getInstance();
    private ToggleGroup roleGroup;
    private boolean passwordVisible = false;
    private boolean repeatPasswordVisible = false;

    @FXML
    public void initialize() {
        errorLabel.setText("");
        
        // Set up toggle group for role selection
        roleGroup = new ToggleGroup();
        adminToggle.setToggleGroup(roleGroup);
        cashierToggle.setToggleGroup(roleGroup);
        userToggle.setToggleGroup(roleGroup);
        
        // Default to User role
        userToggle.setSelected(true);
        
        // Sync password fields
        passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
        repeatPasswordTextField.textProperty().bindBidirectional(repeatPasswordField.textProperty());
    }

    @FXML
    private void togglePasswordVisibility() {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            passwordTextField.setVisible(true);
            passwordTextField.setManaged(true);
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            togglePasswordBtn.setText("Hide");
        } else {
            passwordTextField.setVisible(false);
            passwordTextField.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            togglePasswordBtn.setText("Show");
        }
    }

    @FXML
    private void toggleRepeatPasswordVisibility() {
        repeatPasswordVisible = !repeatPasswordVisible;
        if (repeatPasswordVisible) {
            repeatPasswordTextField.setVisible(true);
            repeatPasswordTextField.setManaged(true);
            repeatPasswordField.setVisible(false);
            repeatPasswordField.setManaged(false);
            toggleRepeatPasswordBtn.setText("Hide");
        } else {
            repeatPasswordTextField.setVisible(false);
            repeatPasswordTextField.setManaged(false);
            repeatPasswordField.setVisible(true);
            repeatPasswordField.setManaged(true);
            toggleRepeatPasswordBtn.setText("Show");
        }
    }

    @FXML
    private void handleCreateAccount() {
        String email = emailField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = passwordField.getText();
        String repeatPassword = repeatPasswordField.getText();

        // Validate inputs
        if (!ValidationUtil.isValidEmail(email)) {
            showError("Please enter a valid email address.");
            return;
        }

        if (!ValidationUtil.isNotEmpty(firstName)) {
            showError("Please enter first name.");
            return;
        }

        if (!ValidationUtil.isNotEmpty(lastName)) {
            showError("Please enter last name.");
            return;
        }

        if (!ValidationUtil.isNotEmpty(phone)) {
            showError("Please enter phone number.");
            return;
        }

        if (!ValidationUtil.isValidPassword(password)) {
            showError("Password must be at least 6 characters.");
            return;
        }

        if (!password.equals(repeatPassword)) {
            showError("Passwords do not match.");
            return;
        }

        // Get selected role
        UserRole role = getSelectedRole();
        if (role == null) {
            showError("Please select a role.");
            return;
        }

        // Check if user already exists
        if (cinemaService.userExists(email)) {
            showError("A user with this email already exists.");
            return;
        }

        // Create the user
        User newUser = new User(email, firstName, lastName, phone, password, role);
        boolean success = cinemaService.registerUser(newUser);

        if (success) {
            GenericSuccessController.setSuccessMessage("The User has been added", "successfully");
            GenericSuccessController.setReturnScene("/fxml/admin_user_management.fxml");
            SceneManager.switchScene("/fxml/generic_success.fxml");
        } else {
            showError("Failed to create user. Please try again.");
        }
    }

    private UserRole getSelectedRole() {
        if (adminToggle.isSelected()) {
            return UserRole.ADMIN;
        } else if (cashierToggle.isSelected()) {
            return UserRole.CASHIER;
        } else if (userToggle.isSelected()) {
            return UserRole.REGULAR_USER;
        }
        return null;
    }

    @FXML
    private void handleBack() {
        SceneManager.switchScene("/fxml/admin_user_management.fxml");
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }
}

