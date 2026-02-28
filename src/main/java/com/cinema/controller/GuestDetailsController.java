package com.cinema.controller;

import com.cinema.model.Screening;
import com.cinema.model.Seat;
import com.cinema.model.Ticket;
import com.cinema.service.CinemaService;
import com.cinema.util.SceneManager;
import com.cinema.util.ValidationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.util.List;

/**
 * Controller for the guest details form.
 * Collects guest information before completing the reservation.
 */
public class GuestDetailsController {

    @FXML
    private Label movieTitleLabel;

    @FXML
    private Label screeningInfoLabel;

    @FXML
    private Label selectedSeatsLabel;

    @FXML
    private Label totalPriceLabel;

    @FXML
    private TextField emailField;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField phoneField;

    @FXML
    private Label errorLabel;

    private final CinemaService cinemaService = CinemaService.getInstance();
    
    // These will be set from the seat selection screen
    private static Screening selectedScreening;
    private static List<Seat> selectedSeats;

    @FXML
    public void initialize() {
        errorLabel.setText("");
        
        // Populate header info from stored data
        if (selectedScreening != null) {
            movieTitleLabel.setText(selectedScreening.getMovie().getTitle());
            screeningInfoLabel.setText(
                selectedScreening.getFormattedDate() + " | " + 
                selectedScreening.getFormattedTime() + " | " + 
                selectedScreening.getHall()
            );
        }
        
        if (selectedSeats != null) {
            int count = selectedSeats.size();
            selectedSeatsLabel.setText("Selected: " + count + " seat" + (count != 1 ? "s" : ""));
            
            double total = count * (selectedScreening != null ? selectedScreening.getPrice() : 0);
            totalPriceLabel.setText(String.format("Total: $%.2f", total));
        }
    }

    /**
     * Sets the screening and seats selected by the guest.
     * Called from SeatSelectionController before navigating here.
     */
    public static void setReservationDetails(Screening screening, List<Seat> seats) {
        selectedScreening = screening;
        selectedSeats = seats;
    }

    @FXML
    private void handleContinue() {
        String email = emailField.getText().trim();
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String phone = phoneField.getText().trim();

        // Validate inputs
        if (!ValidationUtil.isValidEmail(email)) {
            showError("Please enter a valid email address.");
            return;
        }

        if (!ValidationUtil.isNotEmpty(firstName)) {
            showError("Please enter your first name.");
            return;
        }

        if (!ValidationUtil.isNotEmpty(lastName)) {
            showError("Please enter your last name.");
            return;
        }

        if (!ValidationUtil.isNotEmpty(phone)) {
            showError("Please enter your phone number.");
            return;
        }

        // Create the reservation with guest details
        if (selectedScreening != null && selectedSeats != null && !selectedSeats.isEmpty()) {
            // Create ticket with guest name
            Ticket ticket = cinemaService.createTicket(selectedScreening, selectedSeats, firstName, lastName);
            
            if (ticket != null) {
                // Store additional guest info (email and phone)
                String guestInfo = email + " | " + phone;
                ticket.setGuestInfo(guestInfo);
                cinemaService.updateTicket(ticket);
                
                // Navigate to success screen
                SuccessController.setTicketDetails(
                    selectedScreening.getMovie().getTitle(),
                    selectedScreening.getDate().atTime(selectedScreening.getTime()),
                    selectedSeats.size() + " seat(s) reserved",
                    ticket.getTotalPrice()
                );
                SceneManager.switchScene("/fxml/success.fxml");
            } else {
                showError("Failed to create reservation. Please try again.");
            }
        } else {
            showError("No seats selected. Please go back and select seats.");
        }
    }

    @FXML
    private void handleBack() {
        // Navigate back to seat selection with the screening data and previously selected seats
        SeatSelectionController controller = SceneManager.switchSceneAndGetController("/fxml/seat_selection.fxml");
        if (controller != null && selectedScreening != null) {
            // Set the pre-selected seats before setting the screening
            controller.setPreSelectedSeats(selectedSeats);
            controller.setScreening(selectedScreening, null, null);
        }
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }
}
