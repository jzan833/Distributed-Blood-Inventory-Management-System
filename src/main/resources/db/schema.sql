-- ============================================================
-- RedHope Database Schema
-- Database: redhope_db
-- Engine: InnoDB
-- Charset: utf8mb4
-- Collation: utf8mb4_unicode_ci
-- ============================================================

CREATE DATABASE IF NOT EXISTS redhope_db;
USE redhope_db;

-- ------------------------------------------------------------
-- Table: roles
-- Purpose: Reference table for user roles
-- ------------------------------------------------------------
CREATE TABLE roles (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed data
INSERT INTO roles (name, description) VALUES
    ('ROLE_SUPER_ADMIN', 'System-wide administrator with full access'),
    ('ROLE_HOSPITAL_ADMIN', 'Hospital-specific administrator managing inventory and requests'),
    ('ROLE_USER', 'Normal public user - donor or blood requester');

-- ------------------------------------------------------------
-- Table: users
-- Purpose: Central user accounts for all roles
-- ------------------------------------------------------------
CREATE TABLE users (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    blood_type ENUM('A_POSITIVE','A_NEGATIVE','B_POSITIVE','B_NEGATIVE','AB_POSITIVE','AB_NEGATIVE','O_POSITIVE','O_NEGATIVE') NOT NULL,
    city VARCHAR(100) NOT NULL,
    last_donation_date DATE DEFAULT NULL,
    role_id VARCHAR(50) NOT NULL DEFAULT 'ROLE_USER',
    status ENUM('ACTIVE','BANNED','PENDING_VERIFICATION') DEFAULT 'ACTIVE',
    email_verified BOOLEAN DEFAULT FALSE,
    hospital_id BIGINT UNSIGNED DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    INDEX idx_users_email (email),
    INDEX idx_users_city (city),
    INDEX idx_users_blood_type (blood_type),
    INDEX idx_users_role (role_id),
    INDEX idx_users_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- Table: hospitals
-- Purpose: Partner hospital profiles
-- ------------------------------------------------------------
CREATE TABLE hospitals (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    address VARCHAR(500) NOT NULL,
    contact_email VARCHAR(255) NOT NULL,
    contact_phone VARCHAR(20) NOT NULL,
    status ENUM('ACTIVE','SUSPENDED','INACTIVE') DEFAULT 'ACTIVE',
    description VARCHAR(1000) DEFAULT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    INDEX idx_hospitals_city (city),
    INDEX idx_hospitals_status (status),
    UNIQUE KEY uk_hospitals_name_city (name, city)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- Table: blood_inventory
-- Purpose: Live per-hospital blood stock per blood type
-- ------------------------------------------------------------
CREATE TABLE blood_inventory (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    hospital_id BIGINT UNSIGNED NOT NULL,
    blood_type ENUM('A_POSITIVE','A_NEGATIVE','B_POSITIVE','B_NEGATIVE','AB_POSITIVE','AB_NEGATIVE','O_POSITIVE','O_NEGATIVE') NOT NULL,
    units_available INT DEFAULT 0,
    low_stock_threshold INT DEFAULT 5,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    INDEX idx_inventory_hospital (hospital_id),
    INDEX idx_inventory_blood_type (blood_type),
    CONSTRAINT fk_inventory_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- Table: blood_requests
-- Purpose: Blood requests from normal users to hospitals
-- ------------------------------------------------------------
CREATE TABLE blood_requests (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    requester_id BIGINT UNSIGNED NOT NULL,
    hospital_id BIGINT UNSIGNED NOT NULL,
    blood_type ENUM('A_POSITIVE','A_NEGATIVE','B_POSITIVE','B_NEGATIVE','AB_POSITIVE','AB_NEGATIVE','O_POSITIVE','O_NEGATIVE') NOT NULL,
    urgency ENUM('CRITICAL','HIGH','NORMAL') NOT NULL DEFAULT 'NORMAL',
    status ENUM('PENDING','APPROVED','REJECTED','COMPLETED','CANCELLED') DEFAULT 'PENDING',
    medical_reason VARCHAR(2000) NOT NULL,
    doctor_referral_number VARCHAR(100) DEFAULT NULL,
    rejection_reason VARCHAR(1000) DEFAULT NULL,
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    INDEX idx_requests_requester (requester_id),
    INDEX idx_requests_hospital (hospital_id),
    INDEX idx_requests_blood_type (blood_type),
    INDEX idx_requests_urgency (urgency),
    INDEX idx_requests_status (status),
    INDEX idx_requests_requested_at (requested_at),
    CONSTRAINT fk_requests_requester FOREIGN KEY (requester_id) REFERENCES users(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_requests_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ------------------------------------------------------------
-- Table: blood_donations
-- Purpose: Blood donation appointments scheduled by donors
-- ------------------------------------------------------------
CREATE TABLE blood_donations (
    id BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    donor_id BIGINT UNSIGNED NOT NULL,
    hospital_id BIGINT UNSIGNED NOT NULL,
    preferred_date DATE NOT NULL,
    status ENUM('PENDING','APPROVED','REJECTED','COMPLETED','CANCELLED') DEFAULT 'PENDING',
    rejection_reason VARCHAR(1000) DEFAULT NULL,
    health_checklist_passed BOOLEAN DEFAULT FALSE,
    requested_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT NOT NULL DEFAULT 0,
    INDEX idx_donations_donor (donor_id),
    INDEX idx_donations_hospital (hospital_id),
    INDEX idx_donations_preferred_date (preferred_date),
    INDEX idx_donations_status (status),
    CONSTRAINT fk_donations_donor FOREIGN KEY (donor_id) REFERENCES users(id) ON DELETE RESTRICT ON UPDATE CASCADE,
    CONSTRAINT fk_donations_hospital FOREIGN KEY (hospital_id) REFERENCES hospitals(id) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
