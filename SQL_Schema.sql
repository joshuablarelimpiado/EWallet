CREATE DATABASE IF NOT EXISTS ewallet_db;
USE ewallet_db;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    mobile_number VARCHAR(10) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    pin VARCHAR(255) NOT NULL,
    recovery_code VARCHAR(255) NOT NULL,
    balance DOUBLE NOT NULL DEFAULT 0,
    points INT NOT NULL DEFAULT 0,
    is_admin BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    type VARCHAR(100) NOT NULL,
    amount DOUBLE NOT NULL,
    balance_after DOUBLE NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Records every admin action (activate/deactivate/view) for accountability.
CREATE TABLE IF NOT EXISTS admin_audit_log (
    id INT AUTO_INCREMENT PRIMARY KEY,
    admin_id INT NOT NULL,
    action VARCHAR(100) NOT NULL,
    target_user_id INT,
    details VARCHAR(255),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (target_user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Note: the admin account is now seeded automatically by the Java app on
-- startup (see DataStore.seedDefaultAdmin(), called from Main.start()), so
-- a properly BCrypt-hashed admin row is created for you the first time you
-- run the app. Default login: username "admin", password "Admin123!".
-- (SQL alone can't produce a BCrypt hash, so this file intentionally does
-- not insert an admin row anymore -- letting the app do it avoids ending up
-- with an unusable plaintext placeholder row.)

-- ============================================================
-- MIGRATION (run these instead if you already have an existing
-- ewallet_db from before and don't want to drop it):
-- ============================================================
-- ALTER TABLE users ADD COLUMN mobile_number VARCHAR(10) NOT NULL DEFAULT '' UNIQUE;
-- ALTER TABLE users ADD COLUMN recovery_code VARCHAR(255) NOT NULL DEFAULT '';
-- ALTER TABLE users ADD COLUMN points INT NOT NULL DEFAULT 0;
-- ALTER TABLE users ADD COLUMN is_active BOOLEAN NOT NULL DEFAULT TRUE;
-- ALTER TABLE users ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP;
--
-- CREATE TABLE IF NOT EXISTS admin_audit_log (
--     id INT AUTO_INCREMENT PRIMARY KEY,
--     admin_id INT NOT NULL,
--     action VARCHAR(100) NOT NULL,
--     target_user_id INT,
--     details VARCHAR(255),
--     created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
--     FOREIGN KEY (admin_id) REFERENCES users(id) ON DELETE CASCADE,
--     FOREIGN KEY (target_user_id) REFERENCES users(id) ON DELETE SET NULL
-- );
--
-- Existing rows will have empty mobile_number/recovery_code and plaintext
-- passwords that no longer match the hashed login check — for a school
-- project the simplest path is to just re-register test accounts fresh
-- after migrating, rather than trying to backfill real data.
