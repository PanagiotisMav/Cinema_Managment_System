package com.cinema.controller;

import com.cinema.model.User;
import com.cinema.model.UserRole;
import com.cinema.service.CinemaService;
import com.cinema.util.SceneManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Optional;

/**
 * Controller for the Admin User Management screen.
 */
public class AdminUserManagementController {

    @FXML
    private TableView<User> usersTable;

    @FXML
    private TableColumn<User, String> roleColumn;

    @FXML
    private TableColumn<User, String> firstNameColumn;

    @FXML
    private TableColumn<User, String> lastNameColumn;

    @FXML
    private TableColumn<User, String> emailColumn;

    @FXML
    private TableColumn<User, String> phoneColumn;

    @FXML
    private TableColumn<User, Void> deleteColumn;

    private final CinemaService cinemaService = CinemaService.getInstance();
    private final ObservableList<User> usersList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTableColumns();
        loadUsers();
    }

    private void setupTableColumns() {
        roleColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getRole().toString()));
        
        firstNameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getFirstName()));
        
        lastNameColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getLastName()));
        
        emailColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getEmail()));
        
        phoneColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getPhoneNumber()));

        // Delete button column
        deleteColumn.setCellFactory(column -> new TableCell<>() {
            private final Button deleteButton = new Button("✕");
            
            {
                deleteButton.getStyleClass().add("delete-button-small");
                deleteButton.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    handleDeleteUser(user);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(deleteButton);
                }
            }
        });

        usersTable.setItems(usersList);
    }

    private void loadUsers() {
        usersList.clear();
        List<User> users = cinemaService.getAllUsers();
        usersList.addAll(users);
    }

    private void handleDeleteUser(User user) {
        // Don't allow deleting yourself
        User currentUser = cinemaService.getCurrentUser();
        if (currentUser != null && currentUser.getEmail().equals(user.getEmail())) {
            showAlert("Error", "You cannot delete your own account.");
            return;
        }

        // Show confirmation dialog
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Confirm Delete");
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/styles/style.css").toExternalForm());
        dialog.getDialogPane().getStyleClass().add("custom-dialog");

        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #2d3e50;");

        Label messageLabel = new Label("Do you want to Delete the:");
        messageLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");

        // User info display
        HBox userInfo = new HBox(10);
        userInfo.setAlignment(Pos.CENTER);
        userInfo.setStyle("-fx-background-color: white; -fx-padding: 10; -fx-background-radius: 5;");
        
        Label roleLabel = new Label(user.getRole().toString());
        roleLabel.setStyle("-fx-font-weight: bold;");
        Label nameLabel = new Label(user.getFirstName() + " " + user.getLastName());
        Label emailLabel = new Label(user.getEmail());
        Label phoneLabel = new Label(user.getPhoneNumber() != null ? user.getPhoneNumber() : "");
        
        userInfo.getChildren().addAll(roleLabel, nameLabel, emailLabel, phoneLabel);

        content.getChildren().addAll(messageLabel, userInfo);

        dialog.getDialogPane().setContent(content);

        ButtonType noButton = new ButtonType("NO", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType yesButton = new ButtonType("YES", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(noButton, yesButton);

        // Style the buttons
        dialog.getDialogPane().lookupButton(noButton).getStyleClass().add("btn-secondary");
        dialog.getDialogPane().lookupButton(yesButton).getStyleClass().add("btn-danger");

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == yesButton) {
            boolean deleted = cinemaService.deleteUser(user.getEmail());
            if (deleted) {
                // Show success and refresh
                GenericSuccessController.setSuccessMessage("The user has been deleted", "successfully");
                GenericSuccessController.setReturnScene("/fxml/admin_user_management.fxml");
                SceneManager.switchScene("/fxml/generic_success.fxml");
            } else {
                showAlert("Error", "Failed to delete user.");
            }
        }
    }

    @FXML
    private void handleAddUser() {
        SceneManager.switchScene("/fxml/admin_add_user.fxml");
    }

    @FXML
    private void handleBack() {
        SceneManager.switchScene("/fxml/admin_dashboard.fxml");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
