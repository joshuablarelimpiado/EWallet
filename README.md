# E-Wallet Setup Guide

Follow the steps below to set up and run the E-Wallet project on your computer.

---

## Prerequisites

Before starting, make sure you have the following installed:

- Java JDK (version used for the project)
- IntelliJ IDEA
- XAMPP (Apache & MySQL)

---

# 1. Download the Project

1. Download **EWallet.zip** from the repository or from your group member.
2. Right-click the ZIP file and select **Extract All...**
3. Extract it to any folder you like.

Example:

```
C:\Users\YourName\Documents\EWallet
```

> **Important:** Do **NOT** place the project inside the `htdocs` folder. This is a **Java desktop application**, not a PHP website. The project does not run through Apache.

---

# 2. Install and Start XAMPP

If you don't have XAMPP installed:

1. Download it from:
   https://www.apachefriends.org/

2. Install using the default settings.

3. Open the **XAMPP Control Panel**.

4. Start the following services:

- Apache
- MySQL

Both services should turn **green**.

---

# 3. Open phpMyAdmin

Open your browser and go to:

```
http://localhost/phpmyadmin
```

> Apache only needs to be running so phpMyAdmin can open. The E-Wallet application itself is **not** hosted on Apache.

---

# 4. Create the Database

## If `ewallet_db` does NOT exist

1. Open **phpMyAdmin**.
2. Click the **SQL** tab.
3. Open the project's **schema.sql** file.
4. Copy all of its contents.
5. Paste it into phpMyAdmin.
6. Click **Go**.

This will automatically create:

- `ewallet_db`
- `users` table
- `transactions` table
- Default administrator account

---

## If `ewallet_db` already exists

### Option A — Fresh Installation (Recommended)

If you want a clean database:

1. Select **ewallet_db** from the left panel.
2. Open the **Operations** tab.
3. Click **Drop Database**.
4. Confirm the deletion.
5. Run **schema.sql** again.

This recreates the database from scratch.

---

### Option B — Keep Existing Data

If you already have data you want to keep:

Simply run **schema.sql** again.

The script uses:

```sql
CREATE DATABASE IF NOT EXISTS
CREATE TABLE IF NOT EXISTS
```

This means existing tables and data will remain unchanged.

Only recreate the database if the project has a newer database structure (for example, new columns were added) and SQL errors occur.

---

# 5. Open the Project in IntelliJ IDEA

1. Open **IntelliJ IDEA**.
2. Select:

```
File → Open
```

3. Navigate to the extracted **EWallet** folder.
4. Select the project folder.
5. Click **OK**.

If IntelliJ asks whether to import the project as **Maven**, choose:

```
Load Maven Project
```

Wait for Maven to finish downloading all required dependencies.

---

# 6. Configure the Database Connection

Open the database configuration file (if applicable) and verify that the database credentials match your local MySQL server.

Default XAMPP settings are usually:

| Property | Value |
|----------|-------|
| Host | localhost |
| Port | 3306 |
| Database | ewallet_db |
| Username | root |
| Password | *(leave blank)* |

If you changed your MySQL password, update the project configuration accordingly.

---

# 7. Run the Application

1. Open the main application class.
2. Click **Run**.

If everything is configured correctly, the login screen should appear.

---

# Troubleshooting

### Cannot connect to MySQL

- Make sure **MySQL** is running in XAMPP.
- Verify the username and password.
- Ensure `ewallet_db` exists.

---

### phpMyAdmin won't open

- Make sure **Apache** is running.
- Visit:

```
http://localhost/phpmyadmin
```

---

### Maven dependencies won't download

Try:

```
Right Click Project
→ Maven
→ Reload Project
```

or

```
File
→ Sync Project with Maven
```

---

### JavaFX errors

Ensure Maven has finished downloading all project dependencies before running the application.

---

## Notes

- Do **not** move the project into the `htdocs` folder.
- Keep XAMPP's **MySQL** service running while using the application.
- Apache is only required for accessing **phpMyAdmin**.
- If database-related errors occur after pulling a newer version of the project, recreate the database using **Option A** above.
