# E-Wallet

A desktop e-wallet application built with **JavaFX** and **MySQL**. Users can register, log in, deposit/withdraw/transfer funds, earn reward points, and manage their account, while an admin role can view and manage all users. The project also demonstrates **Java Serialization** for session persistence and two **SOLID** design principles applied to the codebase.

## Major Features

- **User registration & login** — sign up with a username, mobile number, password, and PIN; log in with either a username or mobile number.
- **PIN-based quick unlock** — if a session already exists, the app skips straight to a PIN prompt instead of a full re-login.
- **Wallet operations** — deposit, withdraw, and transfer funds between users, with balance and transaction history tracking.
- **Reward points** — points are earned on transactions and tracked per user (`RewardTier`).
- **Forgot password recovery** — reset a forgotten password using a recovery code set at registration.
- **Account settings** — update username/password/PIN, or deactivate (soft-delete) your own account.
- **Admin dashboard** — a seeded admin account can view all registered users, activate/deactivate accounts, and review an audit log of admin actions.
- **Light/dark theme toggle**, persisted across restarts.
- **Session persistence via Java Serialization** (see below), so the app remembers who's logged in between runs.

## Tech Stack

- Java 21, JavaFX 21
- MySQL (via `mysql-connector-j`)
- jBCrypt for password/PIN hashing
- Maven

## Getting Started

1. Run `schema.sql` against your MySQL server (creates the `ewallet_db` database and tables).
2. Run the app once: `mvn clean javafx:run`. On first launch it automatically seeds a working admin account (see `UserRepository.ADMIN_*` constants for the default credentials, or check the console output on first run).
3. Log in as admin, or register your own account from the login screen.

## Session Management via Java Serialization

Logged-in state is persisted to disk using Java's built-in object serialization, rather than an in-memory-only session.

**How it works:**

| Step | Class / Method | What happens |
|---|---|---|
| **Create** | `LoginView` → `SessionManager.save(user)` | On a successful login, the `User` is wrapped in a `UserSession` and written to `session.dat` with `ObjectOutputStream`. |
| **Use / Validate** | `Main.start()` → `SessionManager.load()` | On app startup, `session.dat` is deserialized with `ObjectInputStream`. If a valid session is found, the app goes straight to the PIN-unlock screen instead of full login; if not (or the file is missing/corrupt), it shows the login screen. |
| **Delete** | `SessionManager.clear()`, called from logout, "switch account," and account-deletion flows | `session.dat` is deleted from disk, and the user is redirected back to the login screen (`SceneManager.showLogin()`). |

**Design notes:**

- `UserSession` is a deliberately minimal `Serializable` class — it only stores `id`, `username`, `mobileNumber`, and `admin`. It does **not** serialize the full `User` object, which carries the password/PIN/recovery-code hashes, keeping sensitive data out of the on-disk session file.
- `UserSession` declares a `serialVersionUID`, so an incompatible/old `session.dat` is detected cleanly rather than failing unpredictably.
- `SessionManager.load()` treats a missing or corrupted session file as "no session" (and deletes the bad file) rather than crashing the app.

Relevant files: `SessionManager.java`, `UserSession.java`, `Main.java`, `LoginView.java`, `DashboardView.java`, `PinUnlockView.java`, `SettingsView.java`.

## SOLID Principles Applied

### 1. Single Responsibility Principle (SRP)

**Classes involved:** `DataStore`, `UserRepository`, `TransactionService`, `AdminAuditService`

Data access was originally one large class handling everything — user lookups, registration, wallet transactions, and admin actions. It's now split by responsibility:

- `UserRepository` — user lookups, registration, and account settings.
- `TransactionService` — deposits, withdrawals, transfers, and reward points.
- `AdminAuditService` — admin activation/deactivation actions and the audit log.
- `DataStore` — a thin static facade that delegates to the three classes above, so existing call sites don't need to change.

**Benefit:** each class now has exactly one reason to change. Adjusting reward-point math, for example, only touches `TransactionService`; changing how the audit log is stored only touches `AdminAuditService`. This makes the code easier to test, read, and maintain, and reduces the risk of an unrelated change introducing a bug elsewhere.

### 2. Dependency Inversion Principle (DIP)

**Classes involved:** `UserLookup` (interface), `UserRepository` (implementation), `LoginView`, `PinUnlockView`

`LoginView` and `PinUnlockView` need to look up users during login/unlock, but they depend on the `UserLookup` interface (`findUserByMobile`, `findUserByUsername`, `findUserById`) rather than directly on the concrete `UserRepository` class. `UserRepository` implements `UserLookup` and is injected through the constructor, with a no-arg constructor provided for convenience that wires in the real implementation.

**Benefit:** the view classes are decoupled from the concrete data-access implementation. A different `UserLookup` implementation — an in-memory fake for unit testing, a different database, or a caching layer — could be swapped in without modifying `LoginView` or `PinUnlockView` at all.

<img width="5844" height="9844" alt="image" src="https://github.com/user-attachments/assets/4404f622-ad35-4e9e-91dc-5155949a8aaa" />


