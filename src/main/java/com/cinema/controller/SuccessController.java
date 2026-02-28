package com.cinema.controller;

import com.cinema.model.Ticket;
import com.cinema.util.SceneManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controller for the success/confirmation screen.
 */
public class SuccessController {

    @FXML
    private Label movieNameLabel;

    @FXML
    private Label dateTimeLabel;

    @FXML
    private Label seatsLabel;

    @FXML
    private Label customerLabel;

    @FXML
    private Label priceLabel;

    private Ticket ticket;
    
    // Static fields for guest reservations (set before navigation)
    private static String staticMovieTitle;
    private static LocalDateTime staticDateTime;
    private static String staticSeatsInfo;
    private static double staticTotalPrice;
    private static boolean useStaticData = false;

    @FXML
    public void initialize() {
        // Check if we should use static data (from guest flow)
        if (useStaticData) {
            displayStaticData();
            useStaticData = false; // Reset for next use
        }
    }

    /**
     * Sets ticket details statically for guest reservations.
     * Call this before navigating to the success screen.
     */
    public static void setTicketDetails(String movieTitle, LocalDateTime dateTime, String seatsInfo, double totalPrice) {
        staticMovieTitle = movieTitle;
        staticDateTime = dateTime;
        staticSeatsInfo = seatsInfo;
        staticTotalPrice = totalPrice;
        useStaticData = true;
    }

    private void displayStaticData() {
        if (movieNameLabel != null) {
            movieNameLabel.setText(staticMovieTitle != null ? staticMovieTitle : "");
        }
        if (dateTimeLabel != null && staticDateTime != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy | HH:mm");
            dateTimeLabel.setText(staticDateTime.format(formatter));
        }
        if (seatsLabel != null) {
            seatsLabel.setText(staticSeatsInfo != null ? staticSeatsInfo : "");
        }
        if (customerLabel != null) {
            customerLabel.setText(""); // Hide customer label for guests
        }
        if (priceLabel != null) {
            priceLabel.setText(String.format("Total: $%.2f", staticTotalPrice));
        }
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
        
        if (ticket != null) {
            movieNameLabel.setText(ticket.getScreening().getMovie().getTitle());
            dateTimeLabel.setText(
                    ticket.getScreening().getFormattedDate() + " | " + 
                    ticket.getScreening().getFormattedTime() + " | " +
                    ticket.getScreening().getHall()
            );
            seatsLabel.setText("Seats: " + ticket.getSeatsDisplay());
            customerLabel.setText("Customer: " + ticket.getCustomerFullName());
            priceLabel.setText(String.format("Total: $%.2f", ticket.getTotalPrice()));
        }
    }

    @FXML
    private void handleReturn() {
        SceneManager.switchScene("/fxml/movie_selection.fxml");
    }
}
