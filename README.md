# 🩸 RedHope - Blood Donation & Inventory Management System

RedHope is a web-based platform designed to streamline blood donation workflows, manage hospital blood inventories in real-time, and facilitate urgent blood requests for patients.

---

## 🛠 Tech Stack

* **Backend:** Java 17, Spring Boot 3.x
* **Security:** Spring Security, JWT (JSON Web Tokens)
* **Database:** MariaDB / MySQL
* **ORM & Database Migration:** Spring Data JPA, Hibernate, Native SQL Scripts
* **Build Tool:** Maven

---

## ✨ Features

### 👑 Super Admin
* Manage all partner hospitals and system users.
* Monitor system-wide blood inventory and request statistics.

### 🏥 Hospital Admin
* Real-time blood inventory management (Stock updates, Low-stock alerts).
* Review, approve, or reject blood requests and donation schedules.

### 👤 Normal User (Donor / Requester)
* Search available blood units across partner hospitals.
* Create urgent blood requests with doctor referral credentials.
* Schedule blood donation appointments with preferred dates and health checklists.

---

## 📁 Database Setup

The database schema and initial seed data are provided under `src/main/resources/db/`:

1. **`schema.sql`** - Defines tables for `hospitals`, `users`, `blood_inventory`, `blood_requests`, and `blood_donations`.
2. **`seed.sql`** - Populates essential initial data including default admin accounts and partner hospitals.

To run manually on MariaDB/MySQL:
```sql
CREATE DATABASE redhope_db;
USE redhope_db;
-- Execute schema.sql first, followed by seed.sql
all test password for (user,hospital admin,super admin)--->password123