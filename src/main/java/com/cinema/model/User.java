package com.cinema.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a user in the cinema management system.
 */
public class User {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String password;
    private UserRole role;
    private List<Ticket> tickets;

    public User() {
        this.id = UUID.randomUUID().toString();
        this.tickets = new ArrayList<>();
    }

    public User(String email, String firstName, String lastName, String phoneNumber, String password, UserRole role) {
        this();
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.role = role;
    }

    // Guest user constructor
    public static User createGuest() {
        User guest = new User();
        guest.role = UserRole.GUEST;
        guest.firstName = "Guest";
        guest.lastName = "User";
        return guest;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    public void addTicket(Ticket ticket) {
        this.tickets.add(ticket);
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public List<Ticket> getUsedTickets() {
        return tickets.stream().filter(Ticket::isUsed).toList();
    }

    public List<Ticket> getUnusedTickets() {
        return tickets.stream().filter(t -> !t.isUsed()).toList();
    }
}
