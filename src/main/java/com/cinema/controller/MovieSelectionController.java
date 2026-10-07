package com.cinema.controller;

import com.cinema.model.*;
import com.cinema.service.CinemaService;
import com.cinema.util.SceneManager;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for the movie selection screen.
 */
public class MovieSelectionController {

    @FXML
    private Button backButton;

    @FXML
    private DatePicker datePicker;

    @FXML
    private FlowPane moviesFlowPane;

    @FXML
    private Label noMoviesLabel;

    @FXML
    private Label ticketCountLabel;

    private final CinemaService cinemaService = CinemaService.getInstance();
    private Popup ticketPopup;

    @FXML
    public void initialize() {
        // Set default date to today
        datePicker.setValue(LocalDate.now());
        
        // Update ticket count
        updateTicketCount();
        
        // Load movies for today
        loadMoviesForDate(LocalDate.now());
        
        // Update UI based on user type
        User currentUser = cinemaService.getCurrentUser();
        if (currentUser != null && currentUser.getRole() == UserRole.GUEST) {
            ticketCountLabel.setText("Guest Mode");
        }
    }

    @FXML
    private void handleSearch() {
        LocalDate selectedDate = datePicker.getValue();
        if (selectedDate != null) {
            loadMoviesForDate(selectedDate);
        }
    }

    @FXML
    private void handleLogout() {
        cinemaService.logout();
        SceneManager.switchScene("/fxml/login.fxml");
    }

    @FXML
    private void handleShowTickets() {
        User currentUser = cinemaService.getCurrentUser();
        if (currentUser == null || currentUser.getRole() == UserRole.GUEST) {
            showAlert("Guest Mode", "Ticket history is not available for guest users. Please create an account to track your tickets.");
            return;
        }
        
        showTicketPopup();
    }

    private void loadMoviesForDate(LocalDate date) {
        moviesFlowPane.getChildren().clear();
        
        List<Movie> movies = cinemaService.getMoviesWithScreeningsOnDate(date);
        
        if (movies.isEmpty()) {
            noMoviesLabel.setVisible(true);
            noMoviesLabel.setManaged(true);
        } else {
            noMoviesLabel.setVisible(false);
            noMoviesLabel.setManaged(false);
            
            for (Movie movie : movies) {
                VBox movieCard = createMovieCard(movie, date);
                moviesFlowPane.getChildren().add(movieCard);
            }
        }
    }

    private VBox createMovieCard(Movie movie, LocalDate date) {
        VBox card = new VBox(10);
        card.getStyleClass().add("movie-card");
        card.setAlignment(Pos.TOP_CENTER);
        card.setPrefWidth(220);
        card.setPadding(new Insets(15));

        // Movie poster container
        StackPane posterContainer = new StackPane();
        posterContainer.getStyleClass().add("poster-container");
        posterContainer.setPrefSize(180, 250);
        
        Rectangle posterPlaceholder = new Rectangle(180, 250);
        posterPlaceholder.setFill(Color.web("#2a2a2a"));
        posterPlaceholder.setArcWidth(10);
        posterPlaceholder.setArcHeight(10);
        
        Label posterLabel = new Label("\ud83c\udfac");
        posterLabel.setStyle("-fx-font-size: 48px;");
        
        posterContainer.getChildren().addAll(posterPlaceholder, posterLabel);
        
        // Try to load movie poster from URL
        String posterUrl = movie.getPosterPath();
        if (posterUrl != null && !posterUrl.isEmpty() && posterUrl.startsWith("http")) {
            try {
                ImageView posterImageView = new ImageView();
                posterImageView.setFitWidth(180);
                posterImageView.setFitHeight(250);
                posterImageView.setPreserveRatio(true);
                
                // Create a clipping rectangle for rounded corners
                Rectangle clip = new Rectangle(180, 250);
                clip.setArcWidth(10);
                clip.setArcHeight(10);
                posterImageView.setClip(clip);
                
                Image image = new Image(posterUrl, 180, 250, true, true, true);
                posterImageView.setImage(image);
                
                // Hide placeholder when image loads successfully
                image.progressProperty().addListener((obs, oldProgress, newProgress) -> {
                    if (newProgress.doubleValue() >= 1.0 && !image.isError()) {
                        posterLabel.setVisible(false);
                    }
                });
                
                posterContainer.getChildren().add(posterImageView);
            } catch (Exception e) {
                // Keep placeholder if image fails to load
            }
        }

        // Movie title
        Label titleLabel = new Label(movie.getTitle());
        titleLabel.getStyleClass().add("movie-title");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(180);

        // Movie info
        Label infoLabel = new Label(movie.getFormattedDuration() + " | " + movie.getRating());
        infoLabel.getStyleClass().add("movie-info");

        // Screenings for this date
        VBox screeningsBox = new VBox(5);
        screeningsBox.setAlignment(Pos.CENTER);
        
        Label screeningsTitle = new Label("Showtimes:");
        screeningsTitle.getStyleClass().add("screenings-title");
        screeningsBox.getChildren().add(screeningsTitle);

        FlowPane timesBox = new FlowPane();
        timesBox.setHgap(5);
        timesBox.setVgap(5);
        timesBox.setAlignment(Pos.CENTER);
        timesBox.setPrefWrapLength(180);
        
        List<Screening> screenings = movie.getScreeningsForDate(date);
        for (Screening screening : screenings) {
            Button timeButton = new Button(screening.getFormattedTime());
            timeButton.getStyleClass().add("btn-time");
            timeButton.setOnAction(e -> handleScreeningSelected(screening));
            timesBox.getChildren().add(timeButton);
        }
        
        screeningsBox.getChildren().add(timesBox);

        card.getChildren().addAll(posterContainer, titleLabel, infoLabel, screeningsBox);
        
        return card;
    }

    private void handleScreeningSelected(Screening screening) {
        User currentUser = cinemaService.getCurrentUser();
        
        // All users (including guests) go to seat selection
        // Guests will fill in their details after selecting seats
        navigateToSeatSelection(screening, null, null);
    }

    private void navigateToSeatSelection(Screening screening, String firstName, String lastName) {
        SeatSelectionController controller = SceneManager.switchSceneAndGetController("/fxml/seat_selection.fxml");
        if (controller != null) {
            controller.setScreening(screening, firstName, lastName);
        }
    }

    private void showTicketPopup() {
        if (ticketPopup != null && ticketPopup.isShowing()) {
            ticketPopup.hide();
            return;
        }

        ticketPopup = new Popup();
        
        VBox popupContent = new VBox(10);
        popupContent.getStyleClass().add("ticket-popup");
        popupContent.setPadding(new Insets(20));
        popupContent.setMinWidth(300);

        Label titleLabel = new Label("My Tickets");
        titleLabel.getStyleClass().add("popup-title");

        List<Ticket> tickets = cinemaService.getCurrentUserTickets();
        
        if (tickets.isEmpty()) {
            Label noTicketsLabel = new Label("You don't have any tickets yet.");
            noTicketsLabel.getStyleClass().add("no-tickets-label");
            popupContent.getChildren().addAll(titleLabel, noTicketsLabel);
        } else {
            // Used tickets section
            VBox usedSection = new VBox(5);
            Label usedLabel = new Label("Used Tickets: " + 
                    tickets.stream().filter(Ticket::isUsed).count());
            usedLabel.getStyleClass().add("ticket-section-label");
            usedSection.getChildren().add(usedLabel);

            // Unused tickets section
            VBox unusedSection = new VBox(5);
            Label unusedLabel = new Label("Unused Tickets: " + 
                    tickets.stream().filter(t -> !t.isUsed()).count());
            unusedLabel.getStyleClass().add("ticket-section-label");
            unusedSection.getChildren().add(unusedLabel);

            // List recent tickets
            ScrollPane ticketList = new ScrollPane();
            ticketList.setMaxHeight(200);
            ticketList.getStyleClass().add("ticket-list-scroll");
            
            VBox ticketItems = new VBox(8);
            for (Ticket ticket : tickets.stream().limit(5).toList()) {
                VBox ticketItem = createTicketItem(ticket);
                ticketItems.getChildren().add(ticketItem);
            }
            ticketList.setContent(ticketItems);

            popupContent.getChildren().addAll(titleLabel, usedSection, unusedSection, 
                    new Separator(), ticketList);
        }

        // Close button
        Button closeButton = new Button("Close");
        closeButton.getStyleClass().add("btn-secondary");
        closeButton.setOnAction(e -> ticketPopup.hide());
        popupContent.getChildren().add(closeButton);

        ticketPopup.getContent().add(popupContent);
        
        Stage stage = SceneManager.getPrimaryStage();
        ticketPopup.show(stage, 
                stage.getX() + stage.getWidth() - 350, 
                stage.getY() + 100);
    }

    private VBox createTicketItem(Ticket ticket) {
        VBox item = new VBox(3);
        item.getStyleClass().add("ticket-item");
        item.setPadding(new Insets(8));

        Label movieLabel = new Label(ticket.getScreening().getMovie().getTitle());
        movieLabel.getStyleClass().add("ticket-movie-name");

        Label detailsLabel = new Label(
                ticket.getScreening().getFormattedDate() + " " + 
                ticket.getScreening().getFormattedTime() + " | Seats: " + 
                ticket.getSeatsDisplay());
        detailsLabel.getStyleClass().add("ticket-details");

        Label statusLabel = new Label(ticket.isUsed() ? "✓ Used" : "○ Unused");
        statusLabel.getStyleClass().add(ticket.isUsed() ? "ticket-used" : "ticket-unused");

        item.getChildren().addAll(movieLabel, detailsLabel, statusLabel);
        return item;
    }

    private void updateTicketCount() {
        User currentUser = cinemaService.getCurrentUser();
        if (currentUser != null && currentUser.getRole() != UserRole.GUEST) {
            int count = cinemaService.getCurrentUserTickets().size();
            ticketCountLabel.setText("My Tickets (" + count + ")");
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/styles/style.css").toExternalForm());
        alert.showAndWait();
    }
}
