package com.cinema.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a movie in the cinema system.
 */
public class Movie {
    private String id;
    private String title;
    private String description;
    private String genre;
    private int durationMinutes;
    private String posterPath;
    private String rating;
    private List<Screening> screenings;

    public Movie() {
        this.id = UUID.randomUUID().toString();
        this.screenings = new ArrayList<>();
    }

    public Movie(String title, String description, String genre, int durationMinutes, String posterPath, String rating) {
        this();
        this.title = title;
        this.description = description;
        this.genre = genre;
        this.durationMinutes = durationMinutes;
        this.posterPath = posterPath;
        this.rating = rating;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public List<Screening> getScreenings() {
        return screenings;
    }

    public void setScreenings(List<Screening> screenings) {
        this.screenings = screenings;
    }

    public void addScreening(Screening screening) {
        this.screenings.add(screening);
    }

    public List<Screening> getScreeningsForDate(LocalDate date) {
        return screenings.stream()
                .filter(s -> s.getDate().equals(date))
                .toList();
    }

    public String getFormattedDuration() {
        int hours = durationMinutes / 60;
        int mins = durationMinutes % 60;
        if (hours > 0) {
            return hours + "h " + mins + "min";
        }
        return mins + "min";
    }
}
