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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Controller for the Cashier's movie selection screen.
 */
public class CashierMovieSelectionController {

    @FXML
    private Button backButton;

    @FXML
    private DatePicker datePicker;

    @FXML
    private FlowPane moviesFlowPane;

    @FXML
    private Label noMoviesLabel;

    private final CinemaService cinemaService = CinemaService.getInstance();

    @FXML
    public void initialize() {
        // Set default date to today
        datePicker.setValue(LocalDate.now());
        
        // Load movies for today
        loadMoviesForDate(LocalDate.now());
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
        User currentUser = cinemaService.getCurrentUser();
        
        // If admin, go back to admin dashboard instead of logging out
        if (currentUser != null && currentUser.getRole() == UserRole.ADMIN) {
            SceneManager.switchScene("/fxml/admin_dashboard.fxml");
        } else {
            cinemaService.logout();
            SceneManager.switchScene("/fxml/login.fxml");
        }
    }

    @FXML
    private void handleAddMovie() {
        SceneManager.switchScene("/fxml/add_movie.fxml");
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

        // Delete Movie Button
        Button deleteButton = new Button("Delete Movie");
        deleteButton.getStyleClass().add("btn-delete");
        deleteButton.setOnAction(e -> handleDeleteMovie(movie, screening -> loadMoviesForDate(date)));

        card.getChildren().addAll(posterContainer, titleLabel, infoLabel, screeningsBox, deleteButton);
        
        return card;
    }

    private void handleScreeningSelected(Screening screening) {
        // Navigate to cashier seat selection with reservation list
        CashierSeatSelectionController controller = SceneManager.switchSceneAndGetController("/fxml/cashier_seat_selection.fxml");
        if (controller != null) {
            controller.setScreening(screening);
        }
    }

    private void handleDeleteMovie(Movie movie, java.util.function.Consumer<Void> onDeleted) {
        // Show confirmation dialog
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Delete Movie");
        confirmDialog.setHeaderText("You want to delete the movie:");
        confirmDialog.setContentText(movie.getTitle());
        confirmDialog.getDialogPane().getStylesheets().add(
                getClass().getResource("/styles/style.css").toExternalForm());

        ButtonType yesButton = new ButtonType("YES", ButtonBar.ButtonData.YES);
        ButtonType noButton = new ButtonType("NO", ButtonBar.ButtonData.NO);
        confirmDialog.getButtonTypes().setAll(noButton, yesButton);

        Optional<ButtonType> result = confirmDialog.showAndWait();
        if (result.isPresent() && result.get() == yesButton) {
            boolean deleted = cinemaService.deleteMovie(movie.getId());
            if (deleted) {
                showSuccessAndReturn("The movie has been deleted successfully");
            }
        }
    }

    private void showSuccessAndReturn(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.getDialogPane().getStylesheets().add(
                getClass().getResource("/styles/style.css").toExternalForm());
        alert.showAndWait();
        
        // Refresh the movie list
        loadMoviesForDate(datePicker.getValue());
    }
}
