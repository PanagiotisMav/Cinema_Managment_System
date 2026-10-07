package com.cinema.controller;

import com.cinema.model.*;
import com.cinema.service.CinemaService;
import com.cinema.util.SceneManager;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the Change Ticket screen.
 */
public class ChangeTicketController {

    @FXML
    private Label originalTicketLabel;

    @FXML
    private Label originalScreeningLabel;

    @FXML
    private DatePicker datePicker;

    @FXML
    private VBox screeningsListBox;

    @FXML
    private VBox seatSelectionPanel;

    @FXML
    private Label newScreeningInfoLabel;

    @FXML
    private GridPane seatsGrid;

    @FXML
    private Label selectedSeatsLabel;

    @FXML
    private Button confirmButton;

    private final CinemaService cinemaService = CinemaService.getInstance();
    private Ticket ticketToChange;
    private Screening selectedNewScreening;
    private final List<Seat> selectedSeats = new ArrayList<>();
    private ToggleGroup screeningToggleGroup;

    @FXML
    public void initialize() {
        datePicker.setValue(LocalDate.now());
        screeningToggleGroup = new ToggleGroup();
    }

    public void setTicketToChange(Ticket ticket) {
        this.ticketToChange = ticket;
        
        // Update original ticket info
        originalTicketLabel.setText("Customer: " + ticket.getCustomerFullName());
        originalScreeningLabel.setText(
                ticket.getScreening().getMovie().getTitle() + " - " +
                ticket.getScreening().getFormattedDate() + " - " +
                ticket.getScreening().getFormattedTime()
        );
        
        // Load screenings for today
        loadScreeningsForDate(LocalDate.now());
    }

    @FXML
    private void handleSearch() {
        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate != null) {
            loadScreeningsForDate(selectedDate);
        }
    }

    private void loadScreeningsForDate(LocalDate date) {
        screeningsListBox.getChildren().clear();
        seatSelectionPanel.setVisible(false);
        seatSelectionPanel.setManaged(false);
        selectedNewScreening = null;
        selectedSeats.clear();
        
        List<Movie> movies = cinemaService.getMoviesWithScreeningsOnDate(date);
        
        if (movies.isEmpty()) {
            Label noScreeningsLabel = new Label("No screenings available for this date");
            noScreeningsLabel.getStyleClass().add("no-reservations-label");
            screeningsListBox.getChildren().add(noScreeningsLabel);
        } else {
            for (Movie movie : movies) {
                for (Screening screening : movie.getScreeningsForDate(date)) {
                    VBox screeningCard = createScreeningCard(screening);
                    screeningsListBox.getChildren().add(screeningCard);
                }
            }
        }
    }

    private VBox createScreeningCard(Screening screening) {
        VBox card = new VBox(5);
        card.getStyleClass().add("screening-card");
        card.setPadding(new Insets(12));
        
        // Radio button for selection
        RadioButton radioButton = new RadioButton();
        radioButton.setToggleGroup(screeningToggleGroup);
        radioButton.setOnAction(e -> handleScreeningSelected(screening));
        
        // Movie title
        Label movieLabel = new Label(screening.getMovie().getTitle());
        movieLabel.getStyleClass().add("screening-movie-title");
        
        // Time and hall info
        Label infoLabel = new Label(
                screening.getFormattedTime() + " | " + 
                screening.getHall() + " | $" + 
                String.format("%.2f", screening.getPrice())
        );
        infoLabel.getStyleClass().add("screening-info");
        
        // Available seats count
        int availableCount = screening.getAvailableSeats().size();
        Label availableLabel = new Label(availableCount + " seats available");
        availableLabel.getStyleClass().add("screening-available");
        
        HBox headerBox = new HBox(10);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        headerBox.getChildren().addAll(radioButton, movieLabel);
        
        card.getChildren().addAll(headerBox, infoLabel, availableLabel);
        
        // Make the whole card clickable
        card.setOnMouseClicked(e -> {
            radioButton.setSelected(true);
            handleScreeningSelected(screening);
        });
        
        return card;
    }

    private void handleScreeningSelected(Screening screening) {
        selectedNewScreening = screening;
        selectedSeats.clear();
        
        // Show seat selection panel
        seatSelectionPanel.setVisible(true);
        seatSelectionPanel.setManaged(true);
        
        // Update info
        newScreeningInfoLabel.setText(
                screening.getMovie().getTitle() + " - " +
                screening.getFormattedDate() + " " +
                screening.getFormattedTime()
        );
        
        // Build seat grid
        buildSeatGrid();
        updateSelectionInfo();
    }

    private void buildSeatGrid() {
        seatsGrid.getChildren().clear();
        
        int totalRows = selectedNewScreening.getTotalRows();
        int seatsPerRow = selectedNewScreening.getSeatsPerRow();
        
        for (int row = 0; row < totalRows; row++) {
            char rowLetter = (char) ('A' + row);
            
            // Row label
            Label rowLabel = new Label(String.valueOf(rowLetter));
            rowLabel.getStyleClass().add("row-label-small");
            rowLabel.setMinWidth(20);
            rowLabel.setAlignment(Pos.CENTER);
            seatsGrid.add(rowLabel, 0, row);
            
            // Seats
            HBox rowSeats = new HBox(3);
            rowSeats.setAlignment(Pos.CENTER);
            
            for (int seatNum = 1; seatNum <= seatsPerRow; seatNum++) {
                Seat seat = selectedNewScreening.getSeat(String.valueOf(rowLetter), seatNum);
                ToggleButton seatButton = createSeatButton(seat);
                rowSeats.getChildren().add(seatButton);
            }
            
            seatsGrid.add(rowSeats, 1, row);
        }
    }

    private ToggleButton createSeatButton(Seat seat) {
        ToggleButton button = new ToggleButton(String.valueOf(seat.getSeatNumber()));
        button.setPrefSize(28, 28);
        button.setMinSize(28, 28);
        button.setMaxSize(28, 28);
        button.setStyle("-fx-font-size: 9px;");
        
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
        confirmButton.setDisable(count == 0);
    }

    @FXML
    private void handleConfirmChange() {
        if (selectedNewScreening == null || selectedSeats.isEmpty()) {
            return;
        }
        
        // Perform the ticket change
        Ticket newTicket = cinemaService.changeTicket(
                ticketToChange.getId(), 
                selectedNewScreening, 
                selectedSeats
        );
        
        if (newTicket != null) {
            // Show success with details
            GenericSuccessController controller = SceneManager.switchSceneAndGetController("/fxml/generic_success.fxml");
            if (controller != null) {
                String additionalInfo = "From: " + ticketToChange.getCustomerFullName() + "-" +
                        ticketToChange.getScreening().getMovie().getTitle() + "-" +
                        ticketToChange.getScreening().getFormattedDate() + "\n" +
                        "To: " + newTicket.getScreening().getMovie().getTitle() + "-" +
                        newTicket.getScreening().getFormattedTime() + "-" +
                        newTicket.getScreening().getFormattedDate();
                controller.setMessage("The Change has been deleted successfully", additionalInfo, "/fxml/cashier_movie_selection.fxml");
            }
        }
    }

    @FXML
    private void handleBack() {
        // Go back to the cashier seat selection for the original screening
        CashierSeatSelectionController controller = SceneManager.switchSceneAndGetController("/fxml/cashier_seat_selection.fxml");
        if (controller != null && ticketToChange != null) {
            controller.setScreening(ticketToChange.getScreening());
        }
    }
}
