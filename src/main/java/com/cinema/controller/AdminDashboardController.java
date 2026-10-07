package com.cinema.controller;

import com.cinema.service.CinemaService;
import com.cinema.util.SceneManager;
import javafx.fxml.FXML;

/**
 * Controller for the Admin Dashboard screen.
 */
public class AdminDashboardController {

    private final CinemaService cinemaService = CinemaService.getInstance();

    @FXML
    public void initialize() {
        // Dashboard initialization
    }

    @FXML
    private void handleManageUsers() {
        SceneManager.switchScene("/fxml/admin_user_management.fxml");
    }

    @FXML
    private void handleManageFilms() {
        // Redirect to cashier movie selection (admin has same film management capabilities)
        SceneManager.switchScene("/fxml/cashier_movie_selection.fxml");
    }

    @FXML
    private void handleLogout() {
        cinemaService.logout();
        SceneManager.switchScene("/fxml/login.fxml");
    }
}
