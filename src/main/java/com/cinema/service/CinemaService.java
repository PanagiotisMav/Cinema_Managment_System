package com.cinema.service;

import com.cinema.model.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Service class for managing cinema data (movies, screenings, users).
 * Works with both local data and Firebase when available.
 */
public class CinemaService {
    private static CinemaService instance;
    private final Map<String, User> users;
    private final Map<String, Movie> movies;
    private final Map<String, Screening> screenings;
    private final Map<String, Ticket> tickets;
    private User currentUser;
    private final FirebaseService firebaseService;

    private CinemaService() {
        users = new ConcurrentHashMap<>();
        movies = new ConcurrentHashMap<>();
        screenings = new ConcurrentHashMap<>();
        tickets = new ConcurrentHashMap<>();
        firebaseService = FirebaseService.getInstance();
        
        // Try to initialize Firebase
        firebaseService.initialize();
        
        // Initialize sample data for offline mode
        initializeSampleData();
    }

    public static CinemaService getInstance() {
        if (instance == null) {
            instance = new CinemaService();
        }
        return instance;
    }

    private void initializeSampleData() {
        // Add sample admin user (only to local cache, not Firebase - Firebase will be checked separately)
        User admin = new User("admin@cinema.com", "Admin", "User", "1234567890", "admin123", UserRole.ADMIN);
        users.put(admin.getEmail(), admin);

        // Add sample cashier user (only to local cache)
        User cashier = new User("cashier@cinema.com", "Cashier", "User", "0987654321", "cashier123", UserRole.CASHIER);
        users.put(cashier.getEmail(), cashier);
        
        // Save to Firebase only if they don't exist yet
        if (firebaseService.isInitialized()) {
            firebaseService.saveUserIfNotExists(admin);
            firebaseService.saveUserIfNotExists(cashier);
        }

        // Create sample movies
        createSampleMovies();
    }

    private void createSampleMovies() {
        Movie movie1 = new Movie(
                "Oppenheimer",
                "The story of American scientist J. Robert Oppenheimer and his role in the development of the atomic bomb.",
                "Drama/History",
                180,
                "https://www.themoviedb.org/t/p/w1280/efoCIdMmNgSdOlsNwovGxByjlOR.jpg",
                "R"
        );

        Movie movie2 = new Movie(
                "Captain America: Civil War",
                "Political involvement in the Avengers' affairs causes a rift between Captain America and Iron Man.",
                "Action/Sci-Fi",
                147,
                "https://www.themoviedb.org/t/p/w1280/hkQjebnRQ0XjGRqDPqy9rt4taeI.jpg",
                "PG-13"
        );

        Movie movie3 = new Movie(
                "The Dark Knight",
                "When the menace known as the Joker wreaks havoc on Gotham, Batman must face one of the greatest tests.",
                "Action/Crime",
                152,
                "https://www.themoviedb.org/t/p/w1280/bTOmCkefIK8YNhQNe3IOSueYGNZ.jpg",
                "PG-13"
        );

        // Add screenings for the next 7 days
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 7; i++) {
            LocalDate screeningDate = today.plusDays(i);

            // Movie 1 screenings
            addScreeningToMovie(movie1, screeningDate, LocalTime.of(10, 0), "Hall 1", 12.50);
            addScreeningToMovie(movie1, screeningDate, LocalTime.of(14, 30), "Hall 1", 12.50);
            addScreeningToMovie(movie1, screeningDate, LocalTime.of(19, 0), "Hall 1", 15.00);

            // Movie 2 screenings
            addScreeningToMovie(movie2, screeningDate, LocalTime.of(11, 0), "Hall 2", 11.00);
            addScreeningToMovie(movie2, screeningDate, LocalTime.of(15, 0), "Hall 2", 11.00);
            addScreeningToMovie(movie2, screeningDate, LocalTime.of(20, 0), "Hall 2", 13.50);

            // Movie 3 screenings
            addScreeningToMovie(movie3, screeningDate, LocalTime.of(12, 0), "Hall 3", 10.00);
            addScreeningToMovie(movie3, screeningDate, LocalTime.of(16, 30), "Hall 3", 10.00);
            addScreeningToMovie(movie3, screeningDate, LocalTime.of(21, 0), "Hall 3", 12.00);
        }

        movies.put(movie1.getId(), movie1);
        movies.put(movie2.getId(), movie2);
        movies.put(movie3.getId(), movie3);

        // Save to Firebase only if movies don't already exist
        if (firebaseService.isInitialized()) {
            firebaseService.saveMovieIfNotExists(movie1);
            firebaseService.saveMovieIfNotExists(movie2);
            firebaseService.saveMovieIfNotExists(movie3);
        }
    }

    public Screening addScreeningToMovie(Movie movie, LocalDate date, LocalTime time, String hall, double price) {
        Screening screening = new Screening(movie, date, time, hall, price, 6, 10);
        movie.addScreening(screening);
        screenings.put(screening.getId(), screening);
        return screening;
    }

    // ==================== User Management ====================

    public User login(String email, String password) {
        // First check local cache
        User user = users.get(email);
        if (user != null && user.getPassword().equals(password)) {
            currentUser = user;
            return user;
        }
        
        // If not found locally, try Firebase
        if (firebaseService.isInitialized()) {
            try {
                User firebaseUser = firebaseService.getUserByEmail(email).get(10, java.util.concurrent.TimeUnit.SECONDS);
                if (firebaseUser != null && firebaseUser.getPassword().equals(password)) {
                    // Add to local cache
                    users.put(firebaseUser.getEmail(), firebaseUser);
                    currentUser = firebaseUser;
                    return firebaseUser;
                }
            } catch (Exception e) {
                System.err.println("Error fetching user from Firebase: " + e.getMessage());
            }
        }
        
        return null;
    }

    public User registerUser(String email, String firstName, String lastName, String phoneNumber, String password) {
        if (users.containsKey(email)) {
            return null; // Email already exists
        }
        User newUser = new User(email, firstName, lastName, phoneNumber, password, UserRole.REGULAR_USER);
        users.put(email, newUser);
        currentUser = newUser;

        // Save to Firebase
        if (firebaseService.isInitialized()) {
            firebaseService.saveUser(newUser);
        }

        return newUser;
    }

    public void loginAsGuest() {
        currentUser = User.createGuest();
    }

    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public boolean isEmailRegistered(String email) {
        return users.containsKey(email);
    }

    public boolean userExists(String email) {
        return users.containsKey(email);
    }

    public List<User> getAllUsers() {
        // If Firebase is available, fetch fresh data from Firebase
        if (firebaseService.isInitialized()) {
            List<User> firebaseUsers = firebaseService.fetchAllUsers();
            // Update local cache with Firebase data
            for (User user : firebaseUsers) {
                users.put(user.getEmail(), user);
            }
        }
        
        return new ArrayList<>(users.values()).stream()
                .filter(u -> u.getRole() != UserRole.GUEST)
                .collect(Collectors.toList());
    }

    public boolean deleteUser(String email) {
        User removed = users.remove(email);
        if (removed != null) {
            // Delete from Firebase
            if (firebaseService.isInitialized()) {
                firebaseService.deleteUser(email);
            }
            return true;
        }
        return false;
    }

    public boolean registerUser(User user) {
        if (users.containsKey(user.getEmail())) {
            return false;
        }
        users.put(user.getEmail(), user);
        
        // Save to Firebase
        if (firebaseService.isInitialized()) {
            firebaseService.saveUser(user);
        }
        return true;
    }

    // ==================== Movie Management ====================

    public List<Movie> getAllMovies() {
        // If Firebase is available, fetch fresh data from Firebase
        if (firebaseService.isInitialized()) {
            List<Movie> firebaseMovies = firebaseService.fetchAllMovies();
            // Update local cache with Firebase data
            for (Movie movie : firebaseMovies) {
                if (!movies.containsKey(movie.getId())) {
                    movies.put(movie.getId(), movie);
                }
            }
            // Also fetch screenings for all movies
            List<Screening> firebaseScreenings = firebaseService.fetchAllScreenings(movies);
            for (Screening screening : firebaseScreenings) {
                if (!screenings.containsKey(screening.getId())) {
                    screenings.put(screening.getId(), screening);
                    // Add screening to movie if not already added
                    Movie movie = screening.getMovie();
                    if (movie != null && !movie.getScreenings().contains(screening)) {
                        movie.addScreening(screening);
                    }
                }
            }
        }
        return new ArrayList<>(movies.values());
    }

    public List<Movie> getMoviesWithScreeningsOnDate(LocalDate date) {
        // Ensure we have the latest data from Firebase
        getAllMovies();
        
        return movies.values().stream()
                .filter(m -> !m.getScreeningsForDate(date).isEmpty())
                .collect(Collectors.toList());
    }

    public Movie getMovieById(String movieId) {
        return movies.get(movieId);
    }

    public Movie addMovie(String title, String description, String genre, int durationMinutes, 
                          String posterPath, String rating, LocalDate screeningDate, 
                          LocalTime startTime, LocalTime endTime, String hall, double price) {
        Movie movie = new Movie(title, description, genre, durationMinutes, posterPath, rating);
        
        // Add screening
        Screening screening = new Screening(movie, screeningDate, startTime, hall, price, 6, 10);
        movie.addScreening(screening);
        screenings.put(screening.getId(), screening);
        
        movies.put(movie.getId(), movie);

        // Save to Firebase
        if (firebaseService.isInitialized()) {
            firebaseService.saveMovie(movie);
        }

        return movie;
    }

    public void addScreeningToExistingMovie(String movieId, LocalDate date, LocalTime time, 
                                             String hall, double price) {
        Movie movie = movies.get(movieId);
        if (movie != null) {
            Screening screening = addScreeningToMovie(movie, date, time, hall, price);
            
            // Save to Firebase
            if (firebaseService.isInitialized()) {
                firebaseService.saveScreening(screening);
            }
        }
    }

    public boolean deleteMovie(String movieId) {
        Movie movie = movies.remove(movieId);
        if (movie != null) {
            // Remove all screenings for this movie
            for (Screening screening : movie.getScreenings()) {
                screenings.remove(screening.getId());
                // Remove tickets for this screening
                tickets.values().removeIf(t -> t.getScreening().getId().equals(screening.getId()));
            }

            // Delete from Firebase
            if (firebaseService.isInitialized()) {
                firebaseService.deleteMovie(movieId);
            }
            return true;
        }
        return false;
    }

    public boolean deleteScreening(String screeningId) {
        Screening screening = screenings.remove(screeningId);
        if (screening != null) {
            screening.getMovie().getScreenings().remove(screening);
            // Remove tickets for this screening
            tickets.values().removeIf(t -> t.getScreening().getId().equals(screeningId));
            return true;
        }
        return false;
    }

    // ==================== Screening Management ====================

    public Screening getScreeningById(String screeningId) {
        return screenings.get(screeningId);
    }

    public List<Screening> getAllScreeningsForDate(LocalDate date) {
        return screenings.values().stream()
                .filter(s -> s.getDate().equals(date))
                .collect(Collectors.toList());
    }

    // ==================== Ticket Management ====================

    public Ticket createTicket(Screening screening, List<Seat> selectedSeats, String firstName, String lastName) {
        // Reserve the seats
        for (Seat seat : selectedSeats) {
            Seat screeningSeat = screening.getSeat(seat.getRow(), seat.getSeatNumber());
            if (screeningSeat != null) {
                screeningSeat.reserve();
            }
        }

        Ticket ticket;
        if (currentUser != null && currentUser.getRole() != UserRole.GUEST) {
            ticket = new Ticket(screening, currentUser, selectedSeats);
            currentUser.addTicket(ticket);
        } else {
            ticket = new Ticket(screening, firstName, lastName, selectedSeats);
        }

        tickets.put(ticket.getId(), ticket);

        // Save to Firebase
        if (firebaseService.isInitialized()) {
            firebaseService.saveTicket(ticket);
            firebaseService.updateScreeningSeats(screening);
        }

        return ticket;
    }

    public Ticket createTicketForCustomer(Screening screening, List<Seat> selectedSeats, 
                                           String firstName, String lastName) {
        // Reserve the seats
        for (Seat seat : selectedSeats) {
            Seat screeningSeat = screening.getSeat(seat.getRow(), seat.getSeatNumber());
            if (screeningSeat != null) {
                screeningSeat.reserve();
            }
        }

        Ticket ticket = new Ticket(screening, firstName, lastName, selectedSeats);
        tickets.put(ticket.getId(), ticket);

        // Save to Firebase
        if (firebaseService.isInitialized()) {
            firebaseService.saveTicket(ticket);
            firebaseService.updateScreeningSeats(screening);
        }

        return ticket;
    }

    public List<Ticket> getCurrentUserTickets() {
        if (currentUser != null && currentUser.getRole() != UserRole.GUEST) {
            return currentUser.getTickets();
        }
        return new ArrayList<>();
    }

    public List<Ticket> getTicketsForScreening(String screeningId) {
        return tickets.values().stream()
                .filter(t -> t.getScreening().getId().equals(screeningId))
                .collect(Collectors.toList());
    }

    public Ticket getTicketById(String ticketId) {
        return tickets.get(ticketId);
    }

    public void updateTicket(Ticket ticket) {
        if (ticket != null) {
            tickets.put(ticket.getId(), ticket);
            
            // Update in Firebase
            if (firebaseService.isInitialized()) {
                firebaseService.saveTicket(ticket);
            }
        }
    }

    public Ticket bookTicket(Screening screening, Seat seat) {
        // Reserve the seat
        Seat screeningSeat = screening.getSeat(seat.getRow(), seat.getSeatNumber());
        if (screeningSeat != null && screeningSeat.isAvailable()) {
            screeningSeat.reserve();
        } else {
            return null; // Seat not available
        }

        // Create ticket with single seat
        List<Seat> seats = new ArrayList<>();
        seats.add(seat);
        
        Ticket ticket = new Ticket(screening, "Guest", "User", seats);
        tickets.put(ticket.getId(), ticket);

        // Save to Firebase
        if (firebaseService.isInitialized()) {
            firebaseService.saveTicket(ticket);
            firebaseService.updateScreeningSeats(screening);
        }

        return ticket;
    }

    public boolean cancelTicket(String ticketId) {
        Ticket ticket = tickets.remove(ticketId);
        if (ticket != null) {
            // Release the seats
            Screening screening = ticket.getScreening();
            for (Seat ticketSeat : ticket.getSeats()) {
                Seat screeningSeat = screening.getSeat(ticketSeat.getRow(), ticketSeat.getSeatNumber());
                if (screeningSeat != null) {
                    screeningSeat.release();
                }
            }

            // Remove from user's tickets if applicable
            if (ticket.getUser() != null) {
                ticket.getUser().getTickets().remove(ticket);
            }

            // Update Firebase
            if (firebaseService.isInitialized()) {
                firebaseService.deleteTicket(ticketId);
                firebaseService.updateScreeningSeats(screening);
            }

            return true;
        }
        return false;
    }

    public Ticket changeTicket(String oldTicketId, Screening newScreening, List<Seat> newSeats) {
        Ticket oldTicket = tickets.get(oldTicketId);
        if (oldTicket == null) {
            return null;
        }

        // Get customer info from old ticket
        String firstName = oldTicket.getCustomerFirstName();
        String lastName = oldTicket.getCustomerLastName();
        User user = oldTicket.getUser();

        // Cancel old ticket (releases seats)
        cancelTicket(oldTicketId);

        // Create new ticket with same customer info
        Ticket newTicket = createTicketForCustomer(newScreening, newSeats, firstName, lastName);

        return newTicket;
    }

    // ==================== Firebase Sync ====================

    public FirebaseService getFirebaseService() {
        return firebaseService;
    }

    public boolean isFirebaseConnected() {
        return firebaseService.isInitialized();
    }
}
