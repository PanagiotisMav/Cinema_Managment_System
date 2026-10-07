package com.cinema.service;

import com.cinema.model.*;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.database.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Service class for Firebase Realtime Database operations.
 */
public class FirebaseService {
    private static FirebaseService instance;
    private DatabaseReference database;
    private boolean initialized = false;

    private FirebaseService() {
        // Initialize will be called separately
    }

    public static FirebaseService getInstance() {
        if (instance == null) {
            instance = new FirebaseService();
        }
        return instance;
    }

    /**
     * Initialize Firebase with the service account credentials.
     * The firebase-config.json file should be placed in src/main/resources/
     */
    public void initialize() {
        if (initialized) {
            return;
        }

        try {
            InputStream serviceAccount = getClass().getResourceAsStream("/firebase-config.json");
            
            if (serviceAccount == null) {
                System.err.println("Firebase config file not found. Using offline mode.");
                return;
            }

            // ============================================================
            // FIREBASE REALTIME DATABASE URL
            // ============================================================
            String databaseUrl = "https://cinema-8ab92-default-rtdb.europe-west1.firebasedatabase.app/";
            
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setDatabaseUrl(databaseUrl)
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }

            database = FirebaseDatabase.getInstance().getReference();
            initialized = true;
            System.out.println("Firebase initialized successfully.");

        } catch (IOException e) {
            System.err.println("Failed to initialize Firebase: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    // ==================== User Operations ====================

    public CompletableFuture<Void> saveUser(User user) {
        CompletableFuture<Void> future = new CompletableFuture<>();
        
        if (!initialized) {
            future.complete(null);
            return future;
        }

        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("email", user.getEmail());
        userData.put("firstName", user.getFirstName());
        userData.put("lastName", user.getLastName());
        userData.put("phoneNumber", user.getPhoneNumber());
        userData.put("password", user.getPassword()); // In production, hash this!
        userData.put("role", user.getRole().name());

        database.child("users").child(user.getId()).setValue(userData, (error, ref) -> {
            if (error != null) {
                future.completeExceptionally(new Exception(error.getMessage()));
            } else {
                future.complete(null);
            }
        });

        return future;
    }

    /**
     * Saves a user only if they don't already exist in Firebase (by email).
     */
    public void saveUserIfNotExists(User user) {
        if (!initialized) {
            return;
        }

        // Check if user with this email already exists
        database.child("users").orderByChild("email").equalTo(user.getEmail())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            // User doesn't exist, save them
                            saveUser(user);
                        }
                        // If user exists, do nothing
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Silently fail
                    }
                });
    }

    public CompletableFuture<User> getUserByEmail(String email) {
        CompletableFuture<User> future = new CompletableFuture<>();

        if (!initialized) {
            future.complete(null);
            return future;
        }

        database.child("users").orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                User user = parseUser(userSnapshot);
                                future.complete(user);
                                return;
                            }
                        }
                        future.complete(null);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        future.completeExceptionally(new Exception(error.getMessage()));
                    }
                });

        return future;
    }

    private User parseUser(DataSnapshot snapshot) {
        User user = new User();
        user.setId(snapshot.child("id").getValue(String.class));
        user.setEmail(snapshot.child("email").getValue(String.class));
        user.setFirstName(snapshot.child("firstName").getValue(String.class));
        user.setLastName(snapshot.child("lastName").getValue(String.class));
        user.setPhoneNumber(snapshot.child("phoneNumber").getValue(String.class));
        user.setPassword(snapshot.child("password").getValue(String.class));
        String roleStr = snapshot.child("role").getValue(String.class);
        if (roleStr != null) {
            user.setRole(UserRole.valueOf(roleStr));
        }
        return user;
    }

    public CompletableFuture<Void> deleteUser(String email) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        if (!initialized) {
            future.complete(null);
            return future;
        }

        // Find user by email and delete
        database.child("users").orderByChild("email").equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                            userSnapshot.getRef().removeValue((err, ref) -> {});
                        }
                        future.complete(null);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        future.complete(null);
                    }
                });

        return future;
    }

    /**
     * Fetches all users from Firebase.
     */
    public List<User> fetchAllUsers() {
        List<User> userList = new ArrayList<>();
        
        if (!initialized) {
            return userList;
        }

        CountDownLatch latch = new CountDownLatch(1);

        database.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = parseUser(userSnapshot);
                    if (user != null && user.getEmail() != null) {
                        userList.add(user);
                    }
                }
                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                latch.countDown();
            }
        });

        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return userList;
    }

    // ==================== Movie Operations ====================

    public CompletableFuture<Void> saveMovie(Movie movie) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        if (!initialized) {
            future.complete(null);
            return future;
        }

        Map<String, Object> movieData = new HashMap<>();
        movieData.put("id", movie.getId());
        movieData.put("title", movie.getTitle());
        movieData.put("description", movie.getDescription());
        movieData.put("genre", movie.getGenre());
        movieData.put("durationMinutes", movie.getDurationMinutes());
        movieData.put("posterPath", movie.getPosterPath());
        movieData.put("rating", movie.getRating());

        database.child("movies").child(movie.getId()).setValue(movieData, (error, ref) -> {
            if (error != null) {
                future.completeExceptionally(new Exception(error.getMessage()));
            } else {
                // Save screenings separately
                saveScreeningsForMovie(movie);
                future.complete(null);
            }
        });

        return future;
    }

    /**
     * Saves a movie and its screenings only if a movie with the same title doesn't already exist.
     */
    public void saveMovieIfNotExists(Movie movie) {
        if (!initialized) {
            return;
        }

        // Check if movie with this title already exists
        database.child("movies").orderByChild("title").equalTo(movie.getTitle())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            // Movie doesn't exist, save it
                            saveMovie(movie);
                        }
                        // If movie exists, do nothing
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        // Silently fail
                    }
                });
    }

    private void saveScreeningsForMovie(Movie movie) {
        for (Screening screening : movie.getScreenings()) {
            saveScreening(screening);
        }
    }

    public CompletableFuture<Void> deleteMovie(String movieId) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        if (!initialized) {
            future.complete(null);
            return future;
        }

        // Delete movie and its screenings
        database.child("movies").child(movieId).removeValue((error, ref) -> {
            if (error != null) {
                future.completeExceptionally(new Exception(error.getMessage()));
            } else {
                // Also delete associated screenings
                database.child("screenings").orderByChild("movieId").equalTo(movieId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                for (DataSnapshot screeningSnapshot : snapshot.getChildren()) {
                                    screeningSnapshot.getRef().removeValue((err, ref) -> {});
                                }
                                future.complete(null);
                            }

                            @Override
                            public void onCancelled(DatabaseError error) {
                                future.complete(null);
                            }
                        });
            }
        });

        return future;
    }

    public CompletableFuture<List<Movie>> getAllMovies() {
        CompletableFuture<List<Movie>> future = new CompletableFuture<>();

        if (!initialized) {
            future.complete(new ArrayList<>());
            return future;
        }

        database.child("movies").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                List<Movie> movies = new ArrayList<>();
                for (DataSnapshot movieSnapshot : snapshot.getChildren()) {
                    Movie movie = parseMovie(movieSnapshot);
                    movies.add(movie);
                }
                future.complete(movies);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                future.completeExceptionally(new Exception(error.getMessage()));
            }
        });

        return future;
    }

    private Movie parseMovie(DataSnapshot snapshot) {
        Movie movie = new Movie();
        movie.setId(snapshot.child("id").getValue(String.class));
        movie.setTitle(snapshot.child("title").getValue(String.class));
        movie.setDescription(snapshot.child("description").getValue(String.class));
        movie.setGenre(snapshot.child("genre").getValue(String.class));
        Long duration = snapshot.child("durationMinutes").getValue(Long.class);
        movie.setDurationMinutes(duration != null ? duration.intValue() : 0);
        movie.setPosterPath(snapshot.child("posterPath").getValue(String.class));
        movie.setRating(snapshot.child("rating").getValue(String.class));
        return movie;
    }

    /**
     * Synchronously fetches all movies from Firebase.
     * Similar to fetchAllUsers().
     */
    public List<Movie> fetchAllMovies() {
        List<Movie> movieList = new ArrayList<>();
        
        if (!initialized) {
            return movieList;
        }

        CountDownLatch latch = new CountDownLatch(1);

        database.child("movies").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot movieSnapshot : snapshot.getChildren()) {
                    Movie movie = parseMovie(movieSnapshot);
                    if (movie != null && movie.getId() != null) {
                        movieList.add(movie);
                    }
                }
                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                latch.countDown();
            }
        });

        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return movieList;
    }

    /**
     * Synchronously fetches all screenings from Firebase for a given movies map.
     */
    public List<Screening> fetchAllScreenings(Map<String, Movie> moviesMap) {
        List<Screening> screeningList = new ArrayList<>();
        
        if (!initialized) {
            return screeningList;
        }

        CountDownLatch latch = new CountDownLatch(1);

        database.child("screenings").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                for (DataSnapshot screeningSnapshot : snapshot.getChildren()) {
                    Screening screening = parseScreening(screeningSnapshot, moviesMap);
                    if (screening != null) {
                        screeningList.add(screening);
                    }
                }
                latch.countDown();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                latch.countDown();
            }
        });

        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        return screeningList;
    }

    // ==================== Screening Operations ====================

    public CompletableFuture<Void> saveScreening(Screening screening) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        if (!initialized) {
            future.complete(null);
            return future;
        }

        Map<String, Object> screeningData = new HashMap<>();
        screeningData.put("id", screening.getId());
        screeningData.put("movieId", screening.getMovie().getId());
        screeningData.put("movieTitle", screening.getMovie().getTitle());
        screeningData.put("date", screening.getDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        screeningData.put("time", screening.getTime().format(DateTimeFormatter.ISO_LOCAL_TIME));
        screeningData.put("hall", screening.getHall());
        screeningData.put("price", screening.getPrice());
        screeningData.put("totalRows", screening.getTotalRows());
        screeningData.put("seatsPerRow", screening.getSeatsPerRow());

        // Save reserved seats
        List<String> reservedSeats = new ArrayList<>();
        for (Seat seat : screening.getSeats()) {
            if (!seat.isAvailable()) {
                reservedSeats.add(seat.getSeatLabel());
            }
        }
        screeningData.put("reservedSeats", reservedSeats);

        database.child("screenings").child(screening.getId()).setValue(screeningData, (error, ref) -> {
            if (error != null) {
                future.completeExceptionally(new Exception(error.getMessage()));
            } else {
                future.complete(null);
            }
        });

        return future;
    }

    public CompletableFuture<List<Screening>> getScreeningsForDate(LocalDate date, Map<String, Movie> moviesMap) {
        CompletableFuture<List<Screening>> future = new CompletableFuture<>();

        if (!initialized) {
            future.complete(new ArrayList<>());
            return future;
        }

        String dateStr = date.format(DateTimeFormatter.ISO_LOCAL_DATE);

        database.child("screenings").orderByChild("date").equalTo(dateStr)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<Screening> screenings = new ArrayList<>();
                        for (DataSnapshot screeningSnapshot : snapshot.getChildren()) {
                            Screening screening = parseScreening(screeningSnapshot, moviesMap);
                            if (screening != null) {
                                screenings.add(screening);
                            }
                        }
                        future.complete(screenings);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        future.completeExceptionally(new Exception(error.getMessage()));
                    }
                });

        return future;
    }

    private Screening parseScreening(DataSnapshot snapshot, Map<String, Movie> moviesMap) {
        String movieId = snapshot.child("movieId").getValue(String.class);
        Movie movie = moviesMap.get(movieId);
        
        if (movie == null) {
            return null;
        }

        String dateStr = snapshot.child("date").getValue(String.class);
        String timeStr = snapshot.child("time").getValue(String.class);
        LocalDate date = LocalDate.parse(dateStr, DateTimeFormatter.ISO_LOCAL_DATE);
        LocalTime time = LocalTime.parse(timeStr, DateTimeFormatter.ISO_LOCAL_TIME);

        String hall = snapshot.child("hall").getValue(String.class);
        Double price = snapshot.child("price").getValue(Double.class);
        Long totalRows = snapshot.child("totalRows").getValue(Long.class);
        Long seatsPerRow = snapshot.child("seatsPerRow").getValue(Long.class);

        Screening screening = new Screening(movie, date, time, hall,
                price != null ? price : 10.0,
                totalRows != null ? totalRows.intValue() : 6,
                seatsPerRow != null ? seatsPerRow.intValue() : 10);
        screening.setId(snapshot.child("id").getValue(String.class));

        // Restore reserved seats
        DataSnapshot reservedSeatsSnapshot = snapshot.child("reservedSeats");
        if (reservedSeatsSnapshot.exists()) {
            for (DataSnapshot seatSnapshot : reservedSeatsSnapshot.getChildren()) {
                String seatLabel = seatSnapshot.getValue(String.class);
                if (seatLabel != null && seatLabel.length() >= 2) {
                    String row = seatLabel.substring(0, 1);
                    int seatNum = Integer.parseInt(seatLabel.substring(1));
                    Seat seat = screening.getSeat(row, seatNum);
                    if (seat != null) {
                        seat.reserve();
                    }
                }
            }
        }

        return screening;
    }

    // ==================== Ticket Operations ====================

    public CompletableFuture<Void> saveTicket(Ticket ticket) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        if (!initialized) {
            future.complete(null);
            return future;
        }

        Map<String, Object> ticketData = new HashMap<>();
        ticketData.put("id", ticket.getId());
        ticketData.put("screeningId", ticket.getScreening().getId());
        ticketData.put("movieTitle", ticket.getScreening().getMovie().getTitle());
        ticketData.put("screeningDate", ticket.getScreening().getDate().format(DateTimeFormatter.ISO_LOCAL_DATE));
        ticketData.put("screeningTime", ticket.getScreening().getTime().format(DateTimeFormatter.ISO_LOCAL_TIME));
        ticketData.put("hall", ticket.getScreening().getHall());
        ticketData.put("customerFirstName", ticket.getCustomerFirstName());
        ticketData.put("customerLastName", ticket.getCustomerLastName());
        ticketData.put("totalPrice", ticket.getTotalPrice());
        ticketData.put("used", ticket.isUsed());
        ticketData.put("purchaseTime", ticket.getPurchaseTime().toString());

        if (ticket.getUser() != null) {
            ticketData.put("userId", ticket.getUser().getId());
        }

        // Save seats as list of labels
        List<String> seatLabels = new ArrayList<>();
        for (Seat seat : ticket.getSeats()) {
            seatLabels.add(seat.getSeatLabel());
        }
        ticketData.put("seats", seatLabels);

        database.child("tickets").child(ticket.getId()).setValue(ticketData, (error, ref) -> {
            if (error != null) {
                future.completeExceptionally(new Exception(error.getMessage()));
            } else {
                future.complete(null);
            }
        });

        return future;
    }

    public CompletableFuture<Void> deleteTicket(String ticketId) {
        CompletableFuture<Void> future = new CompletableFuture<>();

        if (!initialized) {
            future.complete(null);
            return future;
        }

        database.child("tickets").child(ticketId).removeValue((error, ref) -> {
            if (error != null) {
                future.completeExceptionally(new Exception(error.getMessage()));
            } else {
                future.complete(null);
            }
        });

        return future;
    }

    public CompletableFuture<List<Ticket>> getTicketsForScreening(String screeningId) {
        CompletableFuture<List<Ticket>> future = new CompletableFuture<>();

        if (!initialized) {
            future.complete(new ArrayList<>());
            return future;
        }

        database.child("tickets").orderByChild("screeningId").equalTo(screeningId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<Ticket> tickets = new ArrayList<>();
                        for (DataSnapshot ticketSnapshot : snapshot.getChildren()) {
                            Ticket ticket = parseTicket(ticketSnapshot);
                            if (ticket != null) {
                                tickets.add(ticket);
                            }
                        }
                        future.complete(tickets);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        future.completeExceptionally(new Exception(error.getMessage()));
                    }
                });

        return future;
    }

    public CompletableFuture<List<Ticket>> getTicketsForUser(String userId) {
        CompletableFuture<List<Ticket>> future = new CompletableFuture<>();

        if (!initialized) {
            future.complete(new ArrayList<>());
            return future;
        }

        database.child("tickets").orderByChild("userId").equalTo(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        List<Ticket> tickets = new ArrayList<>();
                        for (DataSnapshot ticketSnapshot : snapshot.getChildren()) {
                            Ticket ticket = parseTicket(ticketSnapshot);
                            if (ticket != null) {
                                tickets.add(ticket);
                            }
                        }
                        future.complete(tickets);
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        future.completeExceptionally(new Exception(error.getMessage()));
                    }
                });

        return future;
    }

    private Ticket parseTicket(DataSnapshot snapshot) {
        Ticket ticket = new Ticket();
        ticket.setId(snapshot.child("id").getValue(String.class));
        ticket.setCustomerFirstName(snapshot.child("customerFirstName").getValue(String.class));
        ticket.setCustomerLastName(snapshot.child("customerLastName").getValue(String.class));
        Double price = snapshot.child("totalPrice").getValue(Double.class);
        ticket.setTotalPrice(price != null ? price : 0.0);
        Boolean used = snapshot.child("used").getValue(Boolean.class);
        ticket.setUsed(used != null && used);

        // Parse seats
        List<Seat> seats = new ArrayList<>();
        DataSnapshot seatsSnapshot = snapshot.child("seats");
        if (seatsSnapshot.exists()) {
            for (DataSnapshot seatSnapshot : seatsSnapshot.getChildren()) {
                String seatLabel = seatSnapshot.getValue(String.class);
                if (seatLabel != null && seatLabel.length() >= 2) {
                    String row = seatLabel.substring(0, 1);
                    int seatNum = Integer.parseInt(seatLabel.substring(1));
                    Seat seat = new Seat(row, seatNum);
                    seats.add(seat);
                }
            }
        }
        ticket.setSeats(seats);

        return ticket;
    }

    // ==================== Utility Methods ====================

    public void updateScreeningSeats(Screening screening) {
        if (!initialized) return;
        saveScreening(screening);
    }

    /**
     * Initialize default data if database is empty
     */
    public void initializeDefaultData() {
        if (!initialized) return;

        database.child("users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // Add default admin
                    User admin = new User("admin@cinema.com", "Admin", "User", "1234567890", "admin123", UserRole.ADMIN);
                    saveUser(admin);

                    // Add default cashier
                    User cashier = new User("cashier@cinema.com", "Cashier", "User", "0987654321", "cashier123", UserRole.CASHIER);
                    saveUser(cashier);

                    System.out.println("Default users created.");
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                System.err.println("Error checking users: " + error.getMessage());
            }
        });
    }
}
