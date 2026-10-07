package com.cinema.controller;

import com.cinema.model.User;
import com.cinema.service.CinemaService;
import com.cinema.util.SceneManager;
import com.cinema.util.ValidationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller for the sign-up screen.
 */
public class SignUpController {

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
    private PasswordField confirmPasswordField;

    @FXML
    private TextField confirmPasswordTextField;

    @FXML
    private Button toggleConfirmPasswordBtn;

    @FXML
    private Label errorLabel;

    private final CinemaService cinemaService = CinemaService.getInstance();
    private boolean passwordVisible = false;
    private boolean confirmPasswordVisible = false;

    @FXML
    public void initialize() {
        errorLabel.setText("");
        
        // Sync password fields
        passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
        confirmPasswordTextField.textProperty().bindBidirectional(confirmPasswordField.textProperty());
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
    private void toggleConfirmPasswordVisibility() {
        confirmPasswordVisible = !confirmPasswordVisible;
        if (confirmPasswordVisible) {
            confirmPasswordTextField.setVisible(true);
            confirmPasswordTextField.setManaged(true);
            confirmPasswordField.setVisible(false);
            confirmPasswordField.setManaged(false);
            toggleConfirmPasswordBtn.setText("Hide");
        } else {
            confirmPasswordTextField.setVisible(false);
            confirmPasswordTextField.setManaged(false);
            confirmPasswordField.setVisible(true);
            confirmPasswordField.setManaged(true);
            toggleConfirmPasswordBtn.setText("Show");
        }
    }

    @FXML
    private void handleCreateAccount() {
        String email = emailField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        // Validate all inputs
        if (!ValidationUtil.isValidEmail(email)) {
            showError("Please enter a valid email address.");
            return;
        }

        if (cinemaService.isEmailRegistered(email)) {
            showError("This email is already registered. Please use a different email.");
            return;
        }

        if (!ValidationUtil.isValidName(firstName)) {
            showError("Please enter a valid first name (at least 2 characters).");
            return;
        }

        if (!ValidationUtil.isValidName(lastName)) {
            showError("Please enter a valid last name (at least 2 characters).");
            return;
        }

        if (!ValidationUtil.isValidPhone(phone)) {
            showError("Please enter a valid phone number (10-15 digits).");
            return;
        }

        if (!ValidationUtil.isValidPassword(password)) {
            showError("Password must be at least 6 characters long.");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match.");
            return;
        }

        // Create account
        User newUser = cinemaService.registerUser(email, firstName, lastName, phone, password);
        if (newUser != null) {
            navigateToMovieSelection();
        } else {
            showError("Failed to create account. Please try again.");
        }
    }

    @FXML
    private void handleBack() {
        SceneManager.switchScene("/fxml/login.fxml");
    }

    private void navigateToMovieSelection() {
        SceneManager.switchScene("/fxml/movie_selection.fxml");
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }
}

