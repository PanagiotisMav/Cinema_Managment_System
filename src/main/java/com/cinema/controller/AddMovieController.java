package com.cinema.controller;

import com.cinema.model.Movie;
import com.cinema.service.CinemaService;
import com.cinema.util.SceneManager;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the Add Movie screen (Cashier functionality).
 * Supports adding multiple screenings for a single movie.
 */
public class AddMovieController {

    @FXML
    private StackPane imageContainer;

    @FXML
    private ImageView posterPreview;

    @FXML
    private VBox placeholderBox;

    @FXML
    private TextField titleField;

    @FXML
    private TextField imageUrlField;

    @FXML
    private TextField genreField;

    @FXML
    private TextField durationField;

    @FXML
    private ComboBox<String> ratingComboBox;

    @FXML
    private VBox screeningsContainer;

    @FXML
    private Label errorLabel;

    private final CinemaService cinemaService = CinemaService.getInstance();
    private final List<ScreeningEntry> screeningEntries = new ArrayList<>();

    @FXML
    public void initialize() {
        errorLabel.setText("");
        
        // Initialize rating options
        ratingComboBox.getItems().addAll("G", "PG", "PG-13", "R", "NC-17");
        
        // Add one screening entry by default
        addScreeningEntry();
    }

    @FXML
    private void handleAddScreening() {
        addScreeningEntry();
    }

    private void addScreeningEntry() {
        ScreeningEntry entry = new ScreeningEntry();
        screeningEntries.add(entry);
        
        HBox screeningRow = createScreeningRow(entry);
        screeningsContainer.getChildren().add(screeningRow);
    }

    private HBox createScreeningRow(ScreeningEntry entry) {
        HBox row = new HBox(10);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(10));
        row.setStyle("-fx-background-color: rgba(255,255,255,0.05); -fx-background-radius: 5;");

        // Date picker
        VBox dateBox = new VBox(3);
        Label dateLabel = new Label("Date");
        dateLabel.getStyleClass().add("field-label");
        dateLabel.setStyle("-fx-font-size: 11px;");
        DatePicker datePicker = new DatePicker(LocalDate.now().plusDays(1));
        datePicker.setPrefWidth(120);
        datePicker.getStyleClass().add("date-picker-custom");
        entry.datePicker = datePicker;
        dateBox.getChildren().addAll(dateLabel, datePicker);

        // Start time
        VBox startBox = new VBox(3);
        Label startLabel = new Label("Start");
        startLabel.getStyleClass().add("field-label");
        startLabel.setStyle("-fx-font-size: 11px;");
        HBox startTimeBox = new HBox(3);
        startTimeBox.setAlignment(Pos.CENTER_LEFT);
        ComboBox<String> startHour = new ComboBox<>();
        ComboBox<String> startMinute = new ComboBox<>();
        startHour.setPrefWidth(60);
        startMinute.setPrefWidth(60);
        populateTimeComboBoxes(startHour, startMinute);
        entry.startHourCombo = startHour;
        entry.startMinuteCombo = startMinute;
        startTimeBox.getChildren().addAll(startHour, new Label(":"), startMinute);
        startBox.getChildren().addAll(startLabel, startTimeBox);

        // End time
        VBox endBox = new VBox(3);
        Label endLabel = new Label("End");
        endLabel.getStyleClass().add("field-label");
        endLabel.setStyle("-fx-font-size: 11px;");
        HBox endTimeBox = new HBox(3);
        endTimeBox.setAlignment(Pos.CENTER_LEFT);
        ComboBox<String> endHour = new ComboBox<>();
        ComboBox<String> endMinute = new ComboBox<>();
        endHour.setPrefWidth(60);
        endMinute.setPrefWidth(60);
        populateTimeComboBoxes(endHour, endMinute);
        entry.endHourCombo = endHour;
        entry.endMinuteCombo = endMinute;
        endTimeBox.getChildren().addAll(endHour, new Label(":"), endMinute);
        endBox.getChildren().addAll(endLabel, endTimeBox);

        // Hall selection
        VBox hallBox = new VBox(3);
        Label hallLabel = new Label("Hall");
        hallLabel.getStyleClass().add("field-label");
        hallLabel.setStyle("-fx-font-size: 11px;");
        ComboBox<String> hallCombo = new ComboBox<>();
        hallCombo.getItems().addAll("Hall 1", "Hall 2", "Hall 3", "Hall 4", "Hall 5");
        hallCombo.setPrefWidth(90);
        hallCombo.setPromptText("Select");
        entry.hallComboBox = hallCombo;
        hallBox.getChildren().addAll(hallLabel, hallCombo);

        // Price
        VBox priceBox = new VBox(3);
        Label priceLabel = new Label("Price ($)");
        priceLabel.getStyleClass().add("field-label");
        priceLabel.setStyle("-fx-font-size: 11px;");
        TextField priceField = new TextField();
        priceField.setPromptText("12.50");
        priceField.setPrefWidth(70);
        priceField.getStyleClass().add("text-field-custom");
        entry.priceField = priceField;
        priceBox.getChildren().addAll(priceLabel, priceField);

        // Delete button
        Button deleteBtn = new Button("✕");
        deleteBtn.getStyleClass().add("delete-button-small");
        deleteBtn.setOnAction(e -> {
            if (screeningEntries.size() > 1) {
                screeningEntries.remove(entry);
                screeningsContainer.getChildren().remove(row);
            } else {
                showError("You must have at least one screening.");
            }
        });

        row.getChildren().addAll(dateBox, startBox, endBox, hallBox, priceBox, deleteBtn);
        entry.row = row;

        return row;
    }

    private void populateTimeComboBoxes(ComboBox<String> hourCombo, ComboBox<String> minuteCombo) {
        for (int i = 0; i < 24; i++) {
            hourCombo.getItems().add(String.format("%02d", i));
        }
        for (int i = 0; i < 60; i += 15) {
            minuteCombo.getItems().add(String.format("%02d", i));
        }
    }

    @FXML
    private void handlePreviewImage() {
        String imageUrl = imageUrlField.getText().trim();
        if (imageUrl.isEmpty()) {
            showError("Please enter an image URL first.");
            return;
        }
        
        try {
            Image image = new Image(imageUrl, 200, 280, true, true, true);
            image.errorProperty().addListener((obs, oldError, newError) -> {
                if (newError) {
                    showError("Failed to load image. Please check the URL.");
                    placeholderBox.setVisible(true);
                    posterPreview.setImage(null);
                }
            });
            
            if (!image.isError()) {
                posterPreview.setImage(image);
                placeholderBox.setVisible(false);
                errorLabel.setText("");
            }
        } catch (Exception e) {
            showError("Invalid image URL.");
        }
    }

    @FXML
    private void handleAddMovie() {
        // Validate movie details
        String title = titleField.getText().trim();
        String genre = genreField.getText().trim();
        String durationStr = durationField.getText().trim();
        String rating = ratingComboBox.getValue();
        String imageUrl = imageUrlField.getText().trim();

        if (title.isEmpty()) {
            showError("Please enter a movie title.");
            return;
        }

        if (genre.isEmpty()) {
            showError("Please enter a genre.");
            return;
        }

        int duration;
        try {
            duration = Integer.parseInt(durationStr);
            if (duration <= 0) {
                showError("Duration must be a positive number.");
                return;
            }
        } catch (NumberFormatException e) {
            showError("Please enter a valid duration in minutes.");
            return;
        }

        if (rating == null) {
            showError("Please select a rating.");
            return;
        }

        // Validate all screening entries
        List<ScreeningData> validScreenings = new ArrayList<>();
        
        for (int i = 0; i < screeningEntries.size(); i++) {
            ScreeningEntry entry = screeningEntries.get(i);
            int screeningNum = i + 1;

            LocalDate screeningDate = entry.datePicker.getValue();
            if (screeningDate == null) {
                showError("Screening #" + screeningNum + ": Please select a date.");
                return;
            }
            if (screeningDate.isBefore(LocalDate.now())) {
                showError("Screening #" + screeningNum + ": Date cannot be in the past.");
                return;
            }

            String startHour = entry.startHourCombo.getValue();
            String startMinute = entry.startMinuteCombo.getValue();
            if (startHour == null || startMinute == null) {
                showError("Screening #" + screeningNum + ": Please select a start time.");
                return;
            }

            String endHour = entry.endHourCombo.getValue();
            String endMinute = entry.endMinuteCombo.getValue();
            if (endHour == null || endMinute == null) {
                showError("Screening #" + screeningNum + ": Please select an end time.");
                return;
            }

            LocalTime startTime = LocalTime.of(Integer.parseInt(startHour), Integer.parseInt(startMinute));
            LocalTime endTime = LocalTime.of(Integer.parseInt(endHour), Integer.parseInt(endMinute));

            if (!endTime.isAfter(startTime)) {
                showError("Screening #" + screeningNum + ": End time must be after start time.");
                return;
            }

            String hall = entry.hallComboBox.getValue();
            if (hall == null) {
                showError("Screening #" + screeningNum + ": Please select a hall.");
                return;
            }

            String priceStr = entry.priceField.getText().trim();
            double price;
            try {
                price = Double.parseDouble(priceStr);
                if (price <= 0) {
                    showError("Screening #" + screeningNum + ": Price must be positive.");
                    return;
                }
            } catch (NumberFormatException e) {
                showError("Screening #" + screeningNum + ": Please enter a valid price.");
                return;
            }

            validScreenings.add(new ScreeningData(screeningDate, startTime, endTime, hall, price));
        }

        // Create the movie with first screening
        ScreeningData firstScreening = validScreenings.get(0);
        Movie movie = cinemaService.addMovie(
                title,
                "", // description
                genre,
                duration,
                imageUrl,
                rating,
                firstScreening.date,
                firstScreening.startTime,
                firstScreening.endTime,
                firstScreening.hall,
                firstScreening.price
        );

        if (movie != null) {
            // Add additional screenings
            for (int i = 1; i < validScreenings.size(); i++) {
                ScreeningData sd = validScreenings.get(i);
                cinemaService.addScreeningToMovie(movie, sd.date, sd.startTime, sd.hall, sd.price);
            }
            showSuccessScreen();
        } else {
            showError("Failed to add movie. Please try again.");
        }
    }

    @FXML
    private void handleBack() {
        SceneManager.switchScene("/fxml/cashier_movie_selection.fxml");
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }

    private void showSuccessScreen() {
        GenericSuccessController controller = SceneManager.switchSceneAndGetController("/fxml/generic_success.fxml");
        if (controller != null) {
            controller.setMessage("The movie has been added successfully", "/fxml/cashier_movie_selection.fxml");
        }
    }

    // Inner class to hold screening entry UI components
    private static class ScreeningEntry {
        DatePicker datePicker;
        ComboBox<String> startHourCombo;
        ComboBox<String> startMinuteCombo;
        ComboBox<String> endHourCombo;
        ComboBox<String> endMinuteCombo;
        ComboBox<String> hallComboBox;
        TextField priceField;
        HBox row;
    }

    // Inner class to hold validated screening data
    private static class ScreeningData {
        LocalDate date;
        LocalTime startTime;
        LocalTime endTime;
        String hall;
        double price;

        ScreeningData(LocalDate date, LocalTime startTime, LocalTime endTime, String hall, double price) {
            this.date = date;
            this.startTime = startTime;
            this.endTime = endTime;
            this.hall = hall;
            this.price = price;
        }
    }
}
