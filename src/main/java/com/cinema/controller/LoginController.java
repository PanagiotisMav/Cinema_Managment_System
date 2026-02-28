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

/**
 * Controller for the login screen.
 */
public class LoginController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField passwordTextField;

    @FXML
    private Button togglePasswordBtn;

    @FXML
    private Label errorLabel;

    private final CinemaService cinemaService = CinemaService.getInstance();
    private boolean passwordVisible = false;

    @FXML
    public void initialize() {
        errorLabel.setText("");
        
        // Sync password fields
        passwordTextField.textProperty().bindBidirectional(passwordField.textProperty());
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
    private void handleLogin() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        // Validate inputs
        if (!ValidationUtil.isValidEmail(email)) {
            showError("Please enter a valid email address.");
            return;
        }

        if (!ValidationUtil.isNotEmpty(password)) {
            showError("Please enter your password.");
            return;
        }

        // Attempt login
        User user = cinemaService.login(email, password);
        if (user != null) {
            navigateBasedOnRole(user);
        } else {
            showError("Invalid email or password. Please try again.");
        }
    }

    @FXML
    private void handleSignUp() {
        SceneManager.switchScene("/fxml/signup.fxml");
    }

    @FXML
    private void handleGuestLogin() {
        cinemaService.loginAsGuest();
        SceneManager.switchScene("/fxml/movie_selection.fxml");
    }

    private void navigateBasedOnRole(User user) {
        switch (user.getRole()) {
            case CASHIER:
                SceneManager.switchScene("/fxml/cashier_movie_selection.fxml");
                break;
            case ADMIN:
                SceneManager.switchScene("/fxml/admin_dashboard.fxml");
                break;
            case REGULAR_USER:
            default:
                SceneManager.switchScene("/fxml/movie_selection.fxml");
                break;
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }
}
