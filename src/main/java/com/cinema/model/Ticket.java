package com.cinema.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a ticket for a movie screening.
 */
public class Ticket {
    private String id;
    private Screening screening;
    private List<Seat> seats;
    private User user;
    private String customerFirstName;
    private String customerLastName;
    private String guestInfo; // For guest reservations: "Name | Email | Phone"
    private LocalDateTime purchaseTime;
    private double totalPrice;
    private boolean used;

    public Ticket() {
        this.id = UUID.randomUUID().toString();
        this.seats = new ArrayList<>();
        this.purchaseTime = LocalDateTime.now();
        this.used = false;
    }

    public Ticket(Screening screening, User user, List<Seat> seats) {
        this();
        this.screening = screening;
        this.user = user;
        this.seats = new ArrayList<>(seats);
        this.customerFirstName = user.getFirstName();
        this.customerLastName = user.getLastName();
        calculateTotalPrice();
    }

    public Ticket(Screening screening, String firstName, String lastName, List<Seat> seats) {
        this();
        this.screening = screening;
        this.customerFirstName = firstName;
        this.customerLastName = lastName;
        this.seats = new ArrayList<>(seats);
        calculateTotalPrice();
    }

    private void calculateTotalPrice() {
        this.totalPrice = seats.stream()
                .mapToDouble(seat -> screening.getPrice() * seat.getType().getPriceMultiplier())
                .sum();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Screening getScreening() {
        return screening;
    }

    public void setScreening(Screening screening) {
        this.screening = screening;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public void setSeats(List<Seat> seats) {
        this.seats = seats;
        calculateTotalPrice();
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getCustomerFirstName() {
        return customerFirstName;
    }

    public void setCustomerFirstName(String customerFirstName) {
        this.customerFirstName = customerFirstName;
    }

    public String getCustomerLastName() {
        return customerLastName;
    }

    public void setCustomerLastName(String customerLastName) {
        this.customerLastName = customerLastName;
    }

    public LocalDateTime getPurchaseTime() {
        return purchaseTime;
    }

    public void setPurchaseTime(LocalDateTime purchaseTime) {
        this.purchaseTime = purchaseTime;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public String getGuestInfo() {
        return guestInfo;
    }

    public void setGuestInfo(String guestInfo) {
        this.guestInfo = guestInfo;
    }

    public void markAsUsed() {
        this.used = true;
    }

    public String getCustomerFullName() {
        return customerFirstName + " " + customerLastName;
    }

    public String getSeatsDisplay() {
        return seats.stream()
                .map(Seat::getSeatLabel)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }

    public int getSeatCount() {
        return seats.size();
    }
}
