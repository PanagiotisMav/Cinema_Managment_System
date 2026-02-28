# Cinema Management System

A full-featured **desktop application** for cinema management built with **JavaFX** and **Firebase Realtime Database**. The system supports multiple user roles (Admin, Cashier, Regular User, Guest) and provides a complete workflow for movie management, screening scheduling, seat selection, and ticket booking.

![Java](https://img.shields.io/badge/Java-25-orange?style=flat-square&logo=openjdk)
![JavaFX](https://img.shields.io/badge/JavaFX-23-blue?style=flat-square)
![Firebase](https://img.shields.io/badge/Firebase-Realtime%20DB-yellow?style=flat-square&logo=firebase)
![Maven](https://img.shields.io/badge/Maven-Build-red?style=flat-square&logo=apachemaven)

---

## Table of Contents

- [Features](#-features)
- [Tech Stack](#-tech-stack)
- [Architecture](#-architecture)
- [Project Structure](#-project-structure)
- [Prerequisites](#-prerequisites)
- [Installation & Setup](#-installation--setup)
- [Firebase Configuration](#-firebase-configuration)
- [Usage](#-usage)
- [Screenshots](#-screenshots)
- [License](#-license)

---

## Features

### Multi-Role Authentication
- **Admin** — Full system control: manage users, add movies, view dashboard
- **Cashier** — Sell tickets on behalf of customers, manage bookings
- **Regular User** — Browse movies, select screenings & seats, book tickets
- **Guest** — Browse and book without creating an account

### Movie Management
- Add new movies with title, description, genre, duration, poster, and rating
- Automatic screening generation across multiple cinema halls
- Dynamic poster loading from external URLs

### Screening & Seat Selection
- Interactive seat map with **Regular**, **VIP** (1.5×), and **Premium** (2×) seat types
- Real-time seat availability tracking
- Date and time-based screening filtering

### Ticket System
- Book one or multiple seats per transaction
- Automatic price calculation based on seat type
- Ticket modification and cancellation support
- Guest booking with contact details

### Admin Dashboard
- User management (create, view, role assignment)
- Add new movies to the catalog
- System overview and administration tools

### Data Persistence
- **Firebase Realtime Database** for cloud storage and sync
- **Offline mode** with local in-memory data when Firebase is unavailable
- Seamless fallback — the app works fully without an internet connection

---

## Tech Stack

| Technology | Purpose |
|---|---|
| **Java 25** | Core programming language |
| **JavaFX 23** | Desktop UI framework (FXML + CSS) |
| **Firebase Realtime Database** | Cloud data persistence |
| **Firebase Admin SDK 9.2.0** | Server-side Firebase access |
| **Gson 2.10.1** | JSON processing |
| **Maven** | Build & dependency management |
| **SLF4J** | Logging |

---

## Architecture

The project follows the **MVC (Model-View-Controller)** pattern:

```
┌─────────────┐     ┌──────────────┐     ┌─────────────┐
│    Views     │────▶│  Controllers │────▶│  Services   │
│  (FXML/CSS)  │◀────│   (Java)     │◀────│  (Java)     │
└─────────────┘     └──────────────┘     └──────┬──────┘
                                                │
                    ┌──────────────┐             │
                    │    Models    │◀────────────┘
                    │   (Java)    │
                    └──────┬──────┘
                           │
                    ┌──────▼──────┐
                    │  Firebase   │
                    │ Realtime DB │
                    └─────────────┘
```

- **Models** — `User`, `Movie`, `Screening`, `Ticket`, `Seat`, `SeatType`, `UserRole`
- **Views** — 14 FXML screens with a unified dark-themed CSS stylesheet
- **Controllers** — One controller per screen handling UI logic
- **Services** — `CinemaService` (business logic, singleton), `FirebaseService` (database operations)
- **Utilities** — `SceneManager` (navigation), `ValidationUtil` (input validation)

---

## Project Structure

```
src/main/
├── java/com/cinema/
│   ├── CinemaApplication.java        # JavaFX Application entry point
│   ├── Launcher.java                 # Main launcher (non-module)
│   ├── controller/
│   │   ├── LoginController.java
│   │   ├── SignUpController.java
│   │   ├── MovieSelectionController.java
│   │   ├── SeatSelectionController.java
│   │   ├── SuccessController.java
│   │   ├── GuestDetailsController.java
│   │   ├── ChangeTicketController.java
│   │   ├── GenericSuccessController.java
│   │   ├── AddMovieController.java
│   │   ├── AdminDashboardController.java
│   │   ├── AdminAddUserController.java
│   │   ├── AdminUserManagementController.java
│   │   ├── CashierMovieSelectionController.java
│   │   └── CashierSeatSelectionController.java
│   ├── model/
│   │   ├── User.java
│   │   ├── Movie.java
│   │   ├── Screening.java
│   │   ├── Ticket.java
│   │   ├── Seat.java
│   │   ├── SeatType.java              # REGULAR, VIP, PREMIUM
│   │   └── UserRole.java              # ADMIN, CASHIER, REGULAR_USER, GUEST
│   ├── service/
│   │   ├── CinemaService.java         # Core business logic (singleton)
│   │   └── FirebaseService.java       # Firebase Realtime DB operations
│   └── util/
│       ├── SceneManager.java          # Scene navigation utility
│       └── ValidationUtil.java        # Input validation (email, phone, etc.)
└── resources/
    ├── firebase-config.json.template  # Firebase credentials template
    ├── fxml/                          # 14 FXML view files
    ├── images/                        # App icon
    └── styles/
        └── style.css                  # Dark blue & red theme (973 lines)
```

---

## Prerequisites

- **Java 25** (or compatible JDK)
- **Maven 3.6+**
- **Firebase project** (optional — the app works offline without it)

---

## Installation & Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/YOUR_USERNAME/Cinema.git
   cd Cinema
   ```

2. **Build the project**
   ```bash
   mvn clean install
   ```

3. **Run the application**
   ```bash
   mvn javafx:run
   ```

---

## Firebase Configuration

The app supports both **online** (Firebase) and **offline** (in-memory) modes. Firebase is optional.

### To enable Firebase:

1. Go to the [Firebase Console](https://console.firebase.google.com/) and create a project
2. Enable **Realtime Database**
3. Generate a **service account key** (Project Settings → Service Accounts → Generate new private key)
4. Copy `src/main/resources/firebase-config.json.template` to `src/main/resources/firebase-config.json`
5. Paste your service account credentials into `firebase-config.json`

```json
{
  "type": "service_account",
  "project_id": "YOUR_PROJECT_ID",
  "private_key_id": "YOUR_PRIVATE_KEY_ID",
  "private_key": "-----BEGIN PRIVATE KEY-----\n...\n-----END PRIVATE KEY-----\n",
  "client_email": "firebase-adminsdk-xxxxx@YOUR_PROJECT_ID.iam.gserviceaccount.com",
  "client_id": "YOUR_CLIENT_ID",
  ...
}


## Usage

### Default Accounts (Offline / Sample Data)

| Role | Email | Password |
|---|---|---|
| Admin | `admin@cinema.com` | `admin123` |
| Cashier | `cashier@cinema.com` | `cashier123` |

### Workflow

1. **Login** with an existing account, sign up, or continue as guest
2. **Browse movies** — view posters, descriptions, genres, and ratings
3. **Select a screening** — pick a date and time slot
4. **Choose seats** — interactive seat map with Regular/VIP/Premium options
5. **Confirm booking** — review total price and complete the reservation
6. **Admin panel** — manage users, add movies, and oversee the system

