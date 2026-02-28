package com.cinema.model;

/**
 * Represents a seat in a cinema hall.
 */
public class Seat {
    private String row;
    private int seatNumber;
    private boolean available;
    private SeatType type;

    public Seat() {
        this.available = true;
        this.type = SeatType.REGULAR;
    }

    public Seat(String row, int seatNumber) {
        this();
        this.row = row;
        this.seatNumber = seatNumber;
    }

    public Seat(String row, int seatNumber, SeatType type) {
        this(row, seatNumber);
        this.type = type;
    }

    // Getters and Setters
    public String getRow() {
        return row;
    }

    public void setRow(String row) {
        this.row = row;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(int seatNumber) {
        this.seatNumber = seatNumber;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public SeatType getType() {
        return type;
    }

    public void setType(SeatType type) {
        this.type = type;
    }

    public String getSeatLabel() {
        return row + seatNumber;
    }

    public void reserve() {
        this.available = false;
    }

    public void release() {
        this.available = true;
    }

    @Override
    public String toString() {
        return "Seat " + getSeatLabel();
    }
}
