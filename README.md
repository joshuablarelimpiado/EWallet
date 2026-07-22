# E-Wallet

A desktop e-wallet application built with **JavaFX** and **MySQL**. Users can register, log in, deposit/withdraw/transfer funds, earn reward points, and manage their account, while an admin role can view and manage all users. The project also demonstrates **Java Serialization** for session persistence, two **SOLID** design principles, and three **GoF design patterns** (Factory, Adapter, Observer) applied to the codebase.

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
- **Transaction receipts** — every deposit, withdrawal, and transfer generates a viewable receipt (`TransactionDetailView`) with type, amount, resulting balance, and timestamp.

## Tech Stack

- Java 21, JavaFX 21
- MySQL (via `mysql-connector-j`)
- jBCrypt for password/PIN hashing
- Maven

## Getting Started

1. Run `schema.sql` against your MySQL server (creates the `ewallet_db` database and tables).
2. Run the app once: `mvn clean javafx:run`. On first launch it automatically seeds a working admin account (see `UserRepository.ADMIN_*` constants for the default credentials, or check the console output on first run).
3. Log in as admin, or register your own account from the login screen.

## Architecture Overview

The codebase is organized into three layers:

| Layer | Classes | Responsibility |
|---|---|---|
| **UI / View** | `LoginView`, `RegisterView`, `ForgotPasswordView`, `PinUnlockView`, `DashboardView`, `SettingsView`, `TransactionDetailView`, `AdminUserDetailView`, plus `SceneManager`, `ThemeManager`, `ThemeToggleButton`, `FloatingMoneyBackground` | JavaFX screens and navigation. Each view exposes a `getView()` method; `SceneManager` swaps the active `Scene`'s root node to switch screens. Views are stateless — they hold only the data (`User`, `Transaction`) they were constructed with. |
| **Domain / Service** | `User`, `Transaction`, `UserSession`, `RewardTier`, `UserRepository`, `TransactionService`, `AdminAuditService`, `DataStore` | Business logic and data access. `DataStore` is a thin static facade in front of the three service classes (see SOLID section below), so the UI layer only ever calls `DataStore.xxx()`. |
| **Persistence** | `Database`, `SessionManager`, `ThemeManager` (disk persistence), `schema.sql` | `Database` opens JDBC connections to MySQL; `SessionManager` and `ThemeManager` persist small bits of state (login session, theme choice) to local files via Java Serialization. |

### How data flows — example: depositing funds

1. `DashboardView` calls `DataStore.deposit(user, amount)`.
2. `DataStore` delegates to `TransactionService.deposit(...)`.
3. `TransactionService` updates the user's balance and inserts a transaction row in a single JDBC transaction (commit/rollback together), then awards reward points.
4. A `Transaction` object is built via `TransactionFactory` and passed to every registered `TransactionObserver` (currently `ConsoleTransactionLogger`).
5. `DashboardView` re-fetches the user's updated balance/history and refreshes the screen.

The same create → persist → notify → refresh shape applies to withdrawals and transfers; account creation (`registerUser`), updates (`updateUserInfo`, `resetPassword`), and removal (`deactivateUser` — a soft delete that flips `is_active` rather than dropping the row) follow the equivalent path through `UserRepository` instead.

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

## Design Patterns Applied

Three classic Gang-of-Four design patterns are implemented on top of the SOLID refactor above — one creational, one structural, one behavioral.

### 1. Factory Pattern (Creational)

**Class:** `TransactionFactory`

Each kind of money movement (deposit, withdrawal, transfer-sent, transfer-received) needs a `Transaction` object with the right type label and correctly signed amount — e.g. a withdrawal must be stored as `"Cash Out (Withdraw)"` with a **negative** amount, while a deposit is `"Cash In (Deposit)"` with a **positive** amount. `TransactionFactory` centralizes that construction logic into one static method per transaction kind (`createDeposit`, `createWithdrawal`, `createTransferSent`, `createTransferReceived`), instead of leaving each call site in `TransactionService` to hand-format the label and flip the sign itself.

**Benefit:** the rules for "what does a deposit/withdrawal/transfer transaction look like" live in exactly one place. Adding a new transaction kind later means adding one factory method, not hunting through `TransactionService` for every place a `Transaction` gets built.

### 2. Adapter Pattern (Structural)

**Class:** `RowMapper<T>` (interface), implemented as lambda mappers inside `UserRepository` and `TransactionService`

JDBC returns query results as a raw `ResultSet` — a low-level, cursor-based API that the rest of the app shouldn't need to know about. `RowMapper<T>` adapts a `ResultSet` row into a domain object (`User` or `Transaction`) via a single `map(ResultSet): T` method. Both `UserRepository` and `TransactionService` previously had near-identical hand-rolled `mapUser()`/`mapTransaction()` methods; both now delegate to a `RowMapper` implementation instead.

**Benefit:** the incompatible interface (`ResultSet`) is bridged to what the domain layer actually wants (`User`, `Transaction`) through one reusable contract, instead of duplicating row-mapping logic in every repository/service class.

### 3. Observer Pattern (Behavioral)

**Classes:** `TransactionObserver` (interface), `TransactionService` (subject), `ConsoleTransactionLogger` (concrete observer)

`TransactionService` acts as the subject: after a deposit, withdrawal, or transfer commits successfully, it calls `notifyObservers(user, transaction)`, which loops through every registered `TransactionObserver` and invokes `onTransaction(user, transaction)`. `ConsoleTransactionLogger` is the current concrete observer, printing a log line for every completed transaction; it's registered once, in `DataStore`'s static initializer.

**Benefit:** `TransactionService` doesn't need to know what happens after a transaction completes — logging, notifications, analytics, or fraud checks can all be added later as new `TransactionObserver` implementations, registered with `addObserver(...)`, without touching `deposit()`/`withdraw()`/`transfer()` at all.

Relevant files: `TransactionFactory.java`, `RowMapper.java`, `TransactionObserver.java`, `ConsoleTransactionLogger.java`, `TransactionService.java`, `UserRepository.java`.

## UML Class Diagrams

Full class diagrams (domain/service layer and UI/view layer) are in [`docs/EWallet_UML.drawio`](docs/EWallet_UML.drawio) — open with [app.diagrams.net](https://app.diagrams.net) (File → Open From → Device).

<img width="5844" height="9844" alt="image" src="https://github.com/user-attachments/assets/4404f622-ad35-4e9e-91dc-5155949a8aaa" />


