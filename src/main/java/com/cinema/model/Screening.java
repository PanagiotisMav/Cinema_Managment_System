package com.cinema.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a movie screening at a specific date and time.
 */
public class Screening {
    private String id;
    private Movie movie;
    private LocalDate date;
    private LocalTime time;
    private String hall;
    private double price;
    private int totalRows;
    private int seatsPerRow;
    private List<Seat> seats;

    public Screening() {
        this.id = UUID.randomUUID().toString();
        this.seats = new ArrayList<>();
    }

    public Screening(Movie movie, LocalDate date, LocalTime time, String hall, double price, int totalRows, int seatsPerRow) {
        this();
        this.movie = movie;
        this.date = date;
        this.time = time;
        this.hall = hall;
        this.price = price;
        this.totalRows = totalRows;
        this.seatsPerRow = seatsPerRow;
        initializeSeats();
    }

    private void initializeSeats() {
        for (int row = 0; row < totalRows; row++) {
            char rowLetter = (char) ('A' + row);
            for (int seatNum = 1; seatNum <= seatsPerRow; seatNum++) {
                seats.add(new Seat(String.valueOf(rowLetter), seatNum));
            }
        }
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }

    public String getHall() {
        return hall;
    }

    public void setHall(String hall) {
        this.hall = hall;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getTotalRows() {
        return totalRows;
    }

    public void setTotalRows(int totalRows) {
        this.totalRows = totalRows;
    }

    public int getSeatsPerRow() {
        return seatsPerRow;
    }

    public void setSeatsPerRow(int seatsPerRow) {
        this.seatsPerRow = seatsPerRow;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public void setSeats(List<Seat> seats) {
        this.seats = seats;
    }

    public Seat getSeat(String row, int seatNumber) {
        return seats.stream()
                .filter(s -> s.getRow().equals(row) && s.getSeatNumber() == seatNumber)
                .findFirst()
                .orElse(null);
    }

    public List<Seat> getAvailableSeats() {
        return seats.stream()
                .filter(Seat::isAvailable)
                .toList();
    }

    public List<Seat> getReservedSeats() {
        return seats.stream()
                .filter(s -> !s.isAvailable())
                .toList();
    }

    public String getFormattedTime() {
        return time.format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    public String getFormattedDate() {
        return date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
