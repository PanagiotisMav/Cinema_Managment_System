package com.cinema.controller;

import com.cinema.model.*;
import com.cinema.service.CinemaService;
import com.cinema.util.SceneManager;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Controller for the Cashier's seat selection screen with reservation management.
 */
public class CashierSeatSelectionController {

    @FXML
    private Label movieTitleLabel;

    @FXML
    private Label screeningInfoLabel;

    @FXML
    private Label selectedSeatsLabel;

    @FXML
    private Label totalPriceLabel;

    @FXML
    private GridPane seatsGrid;

    @FXML
    private Button bookButton;

    @FXML
    private VBox reservationsListBox;

    @FXML
    private Button changeTicketButton;

    @FXML
    private Button cancelTicketButton;

    private final CinemaService cinemaService = CinemaService.getInstance();
    private Screening currentScreening;
    private final List<Seat> selectedSeats = new ArrayList<>();
    private Ticket selectedTicket = null;
    private ToggleGroup reservationToggleGroup;

    @FXML
    public void initialize() {
        reservationToggleGroup = new ToggleGroup();
    }

    public void setScreening(Screening screening) {
        this.currentScreening = screening;
        
        // Update header info
        movieTitleLabel.setText(screening.getMovie().getTitle());
        screeningInfoLabel.setText(
                screening.getFormattedDate() + " | " + 
                screening.getFormattedTime() + " | " + 
                screening.getHall()
        );
        
        // Build seat grid
        buildSeatGrid();
        updateSelectionInfo();
        
        // Load reservations
        loadReservations();
    }

    private void buildSeatGrid() {
        seatsGrid.getChildren().clear();
        
        int totalRows = currentScreening.getTotalRows();
        int seatsPerRow = currentScreening.getSeatsPerRow();
        
        for (int row = 0; row < totalRows; row++) {
            char rowLetter = (char) ('A' + row);
            
            // Row label on the left
            Label rowLabel = new Label(String.valueOf(rowLetter));
            rowLabel.getStyleClass().add("row-label");
            rowLabel.setMinWidth(25);
            rowLabel.setAlignment(Pos.CENTER);
            seatsGrid.add(rowLabel, 0, row);
            
            // Seats for this row
            HBox rowSeats = new HBox(5);
            rowSeats.setAlignment(Pos.CENTER);
            
            for (int seatNum = 1; seatNum <= seatsPerRow; seatNum++) {
                Seat seat = currentScreening.getSeat(String.valueOf(rowLetter), seatNum);
                ToggleButton seatButton = createSeatButton(seat);
                rowSeats.getChildren().add(seatButton);
            }
            
            seatsGrid.add(rowSeats, 1, row);
            
            // Row label on the right
            Label rowLabelRight = new Label(String.valueOf(rowLetter));
            rowLabelRight.getStyleClass().add("row-label");
            rowLabelRight.setMinWidth(25);
            rowLabelRight.setAlignment(Pos.CENTER);
            seatsGrid.add(rowLabelRight, 2, row);
        }
    }

    private ToggleButton createSeatButton(Seat seat) {
        ToggleButton button = new ToggleButton(String.valueOf(seat.getSeatNumber()));
        button.setPrefSize(32, 32);
        button.setMinSize(32, 32);
        button.setMaxSize(32, 32);
        
        if (seat.isAvailable()) {
            button.getStyleClass().add("seat-button-available");
            button.setOnAction(e -> handleSeatToggle(seat, button));
        } else {
            button.getStyleClass().add("seat-button-reserved");
            button.setDisable(true);
        }
        
        return button;
    }

    private void handleSeatToggle(Seat seat, ToggleButton button) {
        if (button.isSelected()) {
            selectedSeats.add(seat);
            button.getStyleClass().remove("seat-button-available");
            button.getStyleClass().add("seat-button-selected");
        } else {
            selectedSeats.remove(seat);
            button.getStyleClass().remove("seat-button-selected");
            button.getStyleClass().add("seat-button-available");
        }
        updateSelectionInfo();
    }

    private void updateSelectionInfo() {
        int count = selectedSeats.size();
        selectedSeatsLabel.setText("Selected: " + count + " seat" + (count != 1 ? "s" : ""));
        
        double total = count * currentScreening.getPrice();
        totalPriceLabel.setText(String.format("Total: $%.2f", total));
        
        bookButton.setDisable(count == 0);
    }

    private void loadReservations() {
        reservationsListBox.getChildren().clear();
        selectedTicket = null;
        updateActionButtons();
        
        List<Ticket> tickets = cinemaService.getTicketsForScreening(currentScreening.getId());
        
        if (tickets.isEmpty()) {
            Label noReservationsLabel = new Label("No reservations yet");
            noReservationsLabel.getStyleClass().add("no-reservations-label");
            reservationsListBox.getChildren().add(noReservationsLabel);
        } else {
            for (Ticket ticket : tickets) {
                VBox ticketCard = createTicketCard(ticket);
                reservationsListBox.getChildren().add(ticketCard);
            }
        }
    }

    private VBox createTicketCard(Ticket ticket) {
        VBox card = new VBox(5);
        card.getStyleClass().add("reservation-card");
        card.setPadding(new Insets(10));
        
        // Radio button for selection
        RadioButton radioButton = new RadioButton();
        radioButton.setToggleGroup(reservationToggleGroup);
        radioButton.setOnAction(e -> {
            selectedTicket = ticket;
            updateActionButtons();
        });
        
        // Customer name
        Label customerLabel = new Label(ticket.getCustomerFullName());
        customerLabel.getStyleClass().add("reservation-customer");
        
        // Seats
        Label seatsLabel = new Label("Seats: " + ticket.getSeatsDisplay());
        seatsLabel.getStyleClass().add("reservation-seats");
        
        // Price
        Label priceLabel = new Label(String.format("$%.2f", ticket.getTotalPrice()));
        priceLabel.getStyleClass().add("reservation-price");
        
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.getChildren().addAll(radioButton, customerLabel);
        
        card.getChildren().addAll(headerBox, seatsLabel, priceLabel);
        
        // Make the whole card clickable
        card.setOnMouseClicked(e -> {
            radioButton.setSelected(true);
            selectedTicket = ticket;
            updateActionButtons();
        });
        
        return card;
    }

    private void updateActionButtons() {
        boolean hasSelection = selectedTicket != null;
        changeTicketButton.setDisable(!hasSelection);
        cancelTicketButton.setDisable(!hasSelection);
    }

    @FXML
    private void handleBook() {
        if (selectedSeats.isEmpty()) {
            return;
        }
        
        // Show customer details dialog
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Customer Details");
        dialog.setHeaderText("Enter customer information");
        dialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/styles/style.css").toExternalForm());

        // Create the form
        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: #2d3748;");

        TextField firstNameField = new TextField();
        firstNameField.setPromptText("First Name");
        firstNameField.getStyleClass().add("text-field-custom");

        TextField lastNameField = new TextField();
        lastNameField.setPromptText("Last Name");
        lastNameField.getStyleClass().add("text-field-custom");

        Label firstNameLabel = new Label("First Name:");
        firstNameLabel.getStyleClass().add("field-label");
        Label lastNameLabel = new Label("Last Name:");
        lastNameLabel.getStyleClass().add("field-label");

        content.getChildren().addAll(firstNameLabel, firstNameField, lastNameLabel, lastNameField);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                return new String[]{firstNameField.getText().trim(), lastNameField.getText().trim()};
            }
            return null;
        });

        Optional<String[]> result = dialog.showAndWait();
        result.ifPresent(names -> {
            if (names[0].isEmpty() || names[1].isEmpty()) {
                showAlert("Error", "Please enter both first and last name.");
                return;
            }
            
            // Create ticket
            Ticket ticket = cinemaService.createTicketForCustomer(currentScreening, selectedSeats, names[0], names[1]);
            
            // Show success
            GenericSuccessController controller = SceneManager.switchSceneAndGetController("/fxml/generic_success.fxml");
            if (controller != null) {
                controller.setMessage("Your order has been completed successfully", "/fxml/cashier_movie_selection.fxml");
            }
        });
    }

    @FXML
    private void handleChangeTicket() {
        if (selectedTicket == null) {
            return;
        }
        
        // Show confirmation dialog with ticket info
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Change Ticket");
        confirmDialog.setHeaderText("You want to Change the ticket:");
        confirmDialog.setContentText(
                "From: " + selectedTicket.getCustomerFullName() + "-" + 
                selectedTicket.getScreening().getFormattedTime() + "-" +
                selectedTicket.getScreening().getMovie().getTitle() + "-" +
                selectedTicket.getScreening().getFormattedDate()
        );
        confirmDialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/styles/style.css").toExternalForm());

        ButtonType yesButton = new ButtonType("YES", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("NO", ButtonBar.ButtonData.NO);
        confirmDialog.getButtonTypes().setAll(noButton, yesButton);

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == yesButton) {
            // Navigate to change ticket screen
            ChangeTicketController controller = SceneManager.switchSceneAndGetController("/fxml/change_ticket.fxml");
            if (controller != null) {
                controller.setTicketToChange(selectedTicket);
            }
        }
    }

    @FXML
    private void handleCancelTicket() {
        if (selectedTicket == null) {
            return;
        }
        
        // Show confirmation dialog
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Cancel Ticket");
        confirmDialog.setHeaderText("You want to delete the ticket:");
        confirmDialog.setContentText(
                selectedTicket.getCustomerFullName() + " - " + 
                selectedTicket.getScreening().getFormattedTime() + " - " +
                selectedTicket.getScreening().getMovie().getTitle()
        );
        confirmDialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/styles/style.css").toExternalForm());

        ButtonType yesButton = new ButtonType("YES", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("NO", ButtonBar.ButtonData.NO);
        confirmDialog.getButtonTypes().setAll(noButton, yesButton);

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == yesButton) {
            boolean cancelled = cinemaService.cancelTicket(selectedTicket.getId());
            if (cancelled) {
                // Show success
                GenericSuccessController controller = SceneManager.switchSceneAndGetController("/fxml/generic_success.fxml");
                if (controller != null) {
                    controller.setMessage("The ticket has been deleted successfully", "/fxml/cashier_movie_selection.fxml");
                }
            }
        }
    }

    @FXML
    private void handleBack() {
        SceneManager.switchScene("/fxml/cashier_movie_selection.fxml");
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/styles/style.css").toExternalForm());
        alert.showAndWait();
    }
}
