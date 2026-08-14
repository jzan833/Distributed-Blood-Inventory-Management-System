-- ============================================================
-- RedHope Essential Seed Data
-- ============================================================

-- 1. Partner Hospitals
INSERT INTO hospitals (name, city, address, contact_email, contact_phone, status, description) VALUES
('City General Hospital', 'Dhaka', '123 Main Street, Dhaka 1000', 'admin@citygeneral.com', '+880-1711-000001', 'ACTIVE', 'Primary partner hospital in Dhaka city center'),
('Metro Medical Center', 'Chittagong', '456 Coastal Road, Chittagong 4000', 'admin@metromedical.com', '+880-1811-000002', 'ACTIVE', 'Leading medical facility in Chittagong');

-- 2. Initial Blood Inventory
INSERT INTO blood_inventory (hospital_id, blood_type, units_available, low_stock_threshold) VALUES
(1, 'A_POSITIVE', 10, 5),
(1, 'A_NEGATIVE', 5, 5),
(1, 'B_POSITIVE', 8, 5),
(1, 'O_POSITIVE', 12, 5),
(1, 'O_NEGATIVE', 6, 5);

-- 3. Essential Users (Super Admin, Hospital Admin, Normal User)
INSERT INTO users (email, password, full_name, phone, blood_type, city, role_id, status, email_verified, hospital_id) VALUES
('superadmin@redhope.com', '$2a$10$rO8Xz8qJ8qJ8qJ8qJ8qJ8O', 'John Doe', '+880-1700-000000', 'O_POSITIVE', 'Dhaka', 'ROLE_SUPER_ADMIN', 'ACTIVE', TRUE, NULL),
('admin@citygeneral.com', '$2a$10$rO8Xz8qJ8qJ8qJ8qJ8qJ8O', 'Alex Smith', '+880-1711-000001', 'O_POSITIVE', 'Dhaka', 'ROLE_HOSPITAL_ADMIN', 'ACTIVE', TRUE, 1),
('user@redhope.com', '$2a$10$rO8Xz8qJ8qJ8qJ8qJ8qJ8O', 'David Miller', '+880-1711-000010', 'A_POSITIVE', 'Dhaka', 'ROLE_USER', 'ACTIVE', TRUE, NULL);

-- 4. Sample Blood Request
INSERT INTO blood_requests (requester_id, hospital_id, blood_type, urgency, status, medical_reason, doctor_referral_number, requested_at) VALUES
(3, 1, 'A_POSITIVE', 'CRITICAL', 'PENDING', 'Urgent need for surgery patient with severe blood loss requiring immediate A+ blood transfusion.', 'DOC-2024-001', NOW());