package com.cinema.controller;

import com.cinema.model.*;
import com.cinema.service.CinemaService;
import com.cinema.util.SceneManager;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the seat selection screen.
 */
public class SeatSelectionController {

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
    private Button reserveButton;

    private final CinemaService cinemaService = CinemaService.getInstance();
    private Screening currentScreening;
    private String guestFirstName;
    private String guestLastName;
    private final List<Seat> selectedSeats = new ArrayList<>();
    
    // Store pre-selected seats (for when returning from guest details form)
    private List<Seat> preSelectedSeats = null;

    @FXML
    public void initialize() {
        // Will be populated when screening is set
    }

    public void setScreening(Screening screening, String firstName, String lastName) {
        this.currentScreening = screening;
        this.guestFirstName = firstName;
        this.guestLastName = lastName;
        
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
    }
    
    /**
     * Sets pre-selected seats (used when returning from guest details form).
     */
    public void setPreSelectedSeats(List<Seat> seats) {
        this.preSelectedSeats = seats;
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
            rowLabel.setMinWidth(30);
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
            rowLabelRight.setMinWidth(30);
            rowLabelRight.setAlignment(Pos.CENTER);
            seatsGrid.add(rowLabelRight, 2, row);
        }
    }

    private ToggleButton createSeatButton(Seat seat) {
        ToggleButton button = new ToggleButton(String.valueOf(seat.getSeatNumber()));
        button.setPrefSize(35, 35);
        button.setMinSize(35, 35);
        button.setMaxSize(35, 35);
        
        if (seat.isAvailable()) {
            // Check if this seat was pre-selected (returning from guest details)
            boolean isPreSelected = preSelectedSeats != null && 
                preSelectedSeats.stream().anyMatch(s -> 
                    s.getRow().equals(seat.getRow()) && s.getSeatNumber() == seat.getSeatNumber());
            
            if (isPreSelected) {
                button.setSelected(true);
                button.getStyleClass().add("seat-button-selected");
                selectedSeats.add(seat);
            } else {
                button.getStyleClass().add("seat-button-available");
            }
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
        
        reserveButton.setDisable(count == 0);
    }

    @FXML
    private void handleReserve() {
        if (selectedSeats.isEmpty()) {
            return;
        }
        
        User currentUser = cinemaService.getCurrentUser();
        
        // Check if user is a guest - redirect to guest details form
        if (currentUser != null && currentUser.getRole() == UserRole.GUEST) {
            // Store the selected screening and seats for the guest details form
            GuestDetailsController.setReservationDetails(currentScreening, new ArrayList<>(selectedSeats));
            SceneManager.switchScene("/fxml/guest_details.fxml");
            return;
        }
        
        String firstName, lastName;
        
        if (currentUser != null) {
            firstName = currentUser.getFirstName();
            lastName = currentUser.getLastName();
        } else {
            firstName = guestFirstName;
            lastName = guestLastName;
        }
        
        // Create the ticket
        Ticket ticket = cinemaService.createTicket(currentScreening, selectedSeats, firstName, lastName);
        
        // Navigate to success screen
        SuccessController controller = SceneManager.switchSceneAndGetController("/fxml/success.fxml");
        if (controller != null) {
            controller.setTicket(ticket);
        }
    }

    @FXML
    private void handleBack() {
        SceneManager.switchScene("/fxml/movie_selection.fxml");
    }
}
