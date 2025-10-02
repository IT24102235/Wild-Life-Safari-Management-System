-- V3 Consolidated Core Schema + Tourist Preferred Languages (since V1 & V2 are empty)
-- This replaces earlier ad-hoc schema creation. Safe on a fresh database.
-- IF NOT EXISTS guards prevent errors if partially present.

-- Users
IF OBJECT_ID('users','U') IS NULL
BEGIN
    CREATE TABLE users (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        username NVARCHAR(100) NOT NULL UNIQUE,
        email NVARCHAR(150) NOT NULL UNIQUE,
        password_hash NVARCHAR(255) NOT NULL,
        full_name NVARCHAR(200) NOT NULL,
        phone NVARCHAR(50) NOT NULL,
        role NVARCHAR(30) NOT NULL,
        email_verified BIT DEFAULT 0,
        enabled BIT DEFAULT 1,
        locked BIT DEFAULT 0,
        last_login_at DATETIME2 NULL,
        created_by NVARCHAR(50) NULL,
        updated_by NVARCHAR(50) NULL,
        created_at DATETIME2 NOT NULL,
        updated_at DATETIME2 NOT NULL
    );
END;

-- Languages
IF OBJECT_ID('languages','U') IS NULL
BEGIN
    CREATE TABLE languages (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        code NVARCHAR(50) NOT NULL UNIQUE,
        iso_code NVARCHAR(10) NOT NULL UNIQUE,
        name NVARCHAR(150) NOT NULL,
        created_at DATETIME2 NULL,
        updated_at DATETIME2 NULL,
        is_active BIT DEFAULT 1
    );
END;

-- Tour Packages
IF OBJECT_ID('tour_packages','U') IS NULL
BEGIN
    CREATE TABLE tour_packages (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        name NVARCHAR(200) NOT NULL,
        description NVARCHAR(MAX) NULL,
        days INT NOT NULL,
        price DECIMAL(10,2) NOT NULL,
        max_people INT NULL,
        difficulty_level NVARCHAR(100) NULL,
        included_activities NVARCHAR(MAX) NULL,
        is_active BIT DEFAULT 1,
        created_by NVARCHAR(50) NULL,
        updated_by NVARCHAR(50) NULL,
        created_at DATETIME2 NULL,
        updated_at DATETIME2 NULL
    );
END;

-- Tourists
IF OBJECT_ID('tourists','U') IS NULL
BEGIN
    CREATE TABLE tourists (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id BIGINT NOT NULL UNIQUE,
        full_name NVARCHAR(200) NOT NULL,
        passport_number NVARCHAR(100) NULL,
        nationality NVARCHAR(100) NULL,
        emergency_contact NVARCHAR(150) NULL,
        dietary_preferences NVARCHAR(300) NULL,
        created_at DATETIME2 NULL,
        updated_at DATETIME2 NULL,
        CONSTRAINT fk_tourist_user FOREIGN KEY (user_id) REFERENCES users(id)
    );
END;

-- Guides
IF OBJECT_ID('guides','U') IS NULL
BEGIN
    CREATE TABLE guides (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id BIGINT NOT NULL UNIQUE,
        full_name NVARCHAR(200) NOT NULL,
        phone NVARCHAR(50) NOT NULL,
        is_available BIT DEFAULT 1,
        created_by NVARCHAR(50) NULL,
        updated_by NVARCHAR(50) NULL,
        created_at DATETIME2 NULL,
        updated_at DATETIME2 NULL,
        CONSTRAINT fk_guide_user FOREIGN KEY (user_id) REFERENCES users(id)
    );
END;

-- Drivers
IF OBJECT_ID('drivers','U') IS NULL
BEGIN
    CREATE TABLE drivers (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id BIGINT NOT NULL UNIQUE,
        full_name NVARCHAR(200) NOT NULL,
        phone NVARCHAR(50) NOT NULL,
        license_no NVARCHAR(100) NOT NULL UNIQUE,
        is_available BIT DEFAULT 1,
        created_by NVARCHAR(50) NULL,
        updated_by NVARCHAR(50) NULL,
        created_at DATETIME2 NULL,
        updated_at DATETIME2 NULL,
        CONSTRAINT fk_driver_user FOREIGN KEY (user_id) REFERENCES users(id)
    );
END;

-- Mechanics
IF OBJECT_ID('mechanics','U') IS NULL
BEGIN
    CREATE TABLE mechanics (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id BIGINT NOT NULL UNIQUE,
        full_name NVARCHAR(200) NOT NULL,
        phone NVARCHAR(50) NOT NULL,
        skills NVARCHAR(500) NOT NULL,
        is_available BIT DEFAULT 1,
        created_by NVARCHAR(50) NULL,
        updated_by NVARCHAR(50) NULL,
        created_at DATETIME2 NULL,
        updated_at DATETIME2 NULL,
        CONSTRAINT fk_mechanic_user FOREIGN KEY (user_id) REFERENCES users(id)
    );
END;

-- Jeeps
IF OBJECT_ID('jeeps','U') IS NULL
BEGIN
    CREATE TABLE jeeps (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        plate_no NVARCHAR(50) NOT NULL UNIQUE,
        model NVARCHAR(100) NOT NULL,
        capacity INT NOT NULL,
        status NVARCHAR(30) NOT NULL,
        fuel_capacity FLOAT NULL,
        current_mileage FLOAT NULL,
        default_driver_id BIGINT NULL,
        last_maintenance DATETIME2 NULL,
        next_maintenance DATETIME2 NULL,
        created_by NVARCHAR(50) NULL,
        updated_by NVARCHAR(50) NULL,
        created_at DATETIME2 NULL,
        updated_at DATETIME2 NULL,
        CONSTRAINT fk_jeep_default_driver FOREIGN KEY (default_driver_id) REFERENCES drivers(id)
    );
END;

-- Bookings
IF OBJECT_ID('bookings','U') IS NULL
BEGIN
    CREATE TABLE bookings (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        tourist_id BIGINT NOT NULL,
        package_id BIGINT NOT NULL,
        requested_date DATE NOT NULL,
        requested_time TIME NOT NULL,
        status NVARCHAR(30) NOT NULL,
        edit_window_seconds INT NOT NULL,
        payment_window_seconds INT NOT NULL,
        version INT NOT NULL,
        total_amount DECIMAL(10,2) NULL,
        notes NVARCHAR(1000) NULL,
        created_at DATETIME2 NULL,
        updated_at DATETIME2 NULL,
        created_by NVARCHAR(50) NULL,
        updated_by NVARCHAR(50) NULL,
        CONSTRAINT fk_booking_tourist FOREIGN KEY (tourist_id) REFERENCES tourists(id),
        CONSTRAINT fk_booking_package FOREIGN KEY (package_id) REFERENCES tour_packages(id)
    );
END;

-- Payments
IF OBJECT_ID('payments','U') IS NULL
BEGIN
    CREATE TABLE payments (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        booking_id BIGINT NOT NULL,
        amount DECIMAL(10,2) NOT NULL,
        status NVARCHAR(20) NOT NULL,
        method NVARCHAR(50) NULL,
        tx_ref NVARCHAR(100) NULL UNIQUE,
        paid_at DATETIME2 NULL,
        expires_at DATETIME2 NULL,
        created_at DATETIME2 NULL,
        updated_at DATETIME2 NULL,
        created_by NVARCHAR(50) NULL,
        updated_by NVARCHAR(50) NULL,
        CONSTRAINT fk_payment_booking FOREIGN KEY (booking_id) REFERENCES bookings(id)
    );
END;

-- Allocations
IF OBJECT_ID('allocations','U') IS NULL
BEGIN
    CREATE TABLE allocations (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        booking_id BIGINT NOT NULL,
        driver_id BIGINT NULL,
        guide_id BIGINT NULL,
        jeep_id BIGINT NULL,
        status NVARCHAR(20) NOT NULL,
        notes NVARCHAR(500) NULL,
        created_at DATETIME2 NULL,
        updated_at DATETIME2 NULL,
        created_by NVARCHAR(50) NULL,
        updated_by NVARCHAR(50) NULL,
        CONSTRAINT fk_allocation_booking FOREIGN KEY (booking_id) REFERENCES bookings(id),
        CONSTRAINT fk_allocation_driver FOREIGN KEY (driver_id) REFERENCES drivers(id),
        CONSTRAINT fk_allocation_guide FOREIGN KEY (guide_id) REFERENCES guides(id),
        CONSTRAINT fk_allocation_jeep FOREIGN KEY (jeep_id) REFERENCES jeeps(id)
    );
END;

-- Maintenance Tickets
IF OBJECT_ID('maintenance_tickets','U') IS NULL
BEGIN
    CREATE TABLE maintenance_tickets (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        vehicle_id BIGINT NOT NULL,
        filed_by_user_id BIGINT NOT NULL,
        assignee_mechanic_id BIGINT NULL,
        status NVARCHAR(20) NOT NULL,
        severity NVARCHAR(20) NOT NULL,
        title NVARCHAR(200) NOT NULL,
        description NVARCHAR(1000) NOT NULL,
        resolution_notes NVARCHAR(1000) NULL,
        opened_at DATETIME2 NOT NULL,
        closed_at DATETIME2 NULL,
        created_at DATETIME2 NULL,
        updated_at DATETIME2 NULL,
        created_by NVARCHAR(50) NULL,
        updated_by NVARCHAR(50) NULL,
        CONSTRAINT fk_ticket_vehicle FOREIGN KEY (vehicle_id) REFERENCES jeeps(id),
        CONSTRAINT fk_ticket_filed_user FOREIGN KEY (filed_by_user_id) REFERENCES users(id),
        CONSTRAINT fk_ticket_mechanic FOREIGN KEY (assignee_mechanic_id) REFERENCES mechanics(id)
    );
END;

-- Guide Languages
IF OBJECT_ID('guide_languages','U') IS NULL
BEGIN
    CREATE TABLE guide_languages (
        guide_id BIGINT NOT NULL,
        language_id BIGINT NOT NULL,
        CONSTRAINT pk_guide_languages PRIMARY KEY (guide_id, language_id),
        CONSTRAINT fk_gl_guide FOREIGN KEY (guide_id) REFERENCES guides(id) ON DELETE CASCADE,
        CONSTRAINT fk_gl_language FOREIGN KEY (language_id) REFERENCES languages(id) ON DELETE CASCADE
    );
END;

-- Tourist Preferred Languages
IF OBJECT_ID('tourist_preferred_languages','U') IS NULL
BEGIN
    CREATE TABLE tourist_preferred_languages (
        tourist_id BIGINT NOT NULL,
        language_id BIGINT NOT NULL,
        CONSTRAINT uq_tpl_tourist_language UNIQUE (tourist_id, language_id),
        CONSTRAINT fk_tpl_tourist FOREIGN KEY (tourist_id) REFERENCES tourists(id) ON DELETE CASCADE,
        CONSTRAINT fk_tpl_language FOREIGN KEY (language_id) REFERENCES languages(id) ON DELETE CASCADE
    );
    CREATE INDEX idx_tpl_tourist ON tourist_preferred_languages(tourist_id);
    CREATE INDEX idx_tpl_language ON tourist_preferred_languages(language_id);
END;

-- Audit Log
IF OBJECT_ID('audit_log','U') IS NULL
BEGIN
    CREATE TABLE audit_log (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        actor_user_id BIGINT NULL,
        entity NVARCHAR(50) NOT NULL,
        entity_id NVARCHAR(50) NOT NULL,
        action NVARCHAR(20) NOT NULL,
        before_json NVARCHAR(MAX) NULL,
        after_json NVARCHAR(MAX) NULL,
        created_at DATETIME2 NOT NULL,
        CONSTRAINT fk_audit_actor_user FOREIGN KEY (actor_user_id) REFERENCES users(id)
    );
END;

-- Outbound Emails
IF OBJECT_ID('outbound_emails','U') IS NULL
BEGIN
    CREATE TABLE outbound_emails (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        to_email NVARCHAR(100) NOT NULL,
        subject NVARCHAR(200) NOT NULL,
        body NVARCHAR(MAX) NOT NULL,
        template_name NVARCHAR(50) NULL,
        sent_at DATETIME2 NULL,
        status NVARCHAR(20) NOT NULL,
        error_message NVARCHAR(500) NULL,
        created_at DATETIME2 NOT NULL
    );
END;

-- Notifications
IF OBJECT_ID('notifications','U') IS NULL
BEGIN
    CREATE TABLE notifications (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id BIGINT NOT NULL,
        type NVARCHAR(50) NOT NULL,
        title NVARCHAR(200) NOT NULL,
        body NVARCHAR(1000) NOT NULL,
        read_at DATETIME2 NULL,
        created_at DATETIME2 NOT NULL,
        CONSTRAINT fk_notification_user FOREIGN KEY (user_id) REFERENCES users(id)
    );
END;

-- OTPs
IF OBJECT_ID('otps','U') IS NULL
BEGIN
    CREATE TABLE otps (
        id BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id BIGINT NOT NULL,
        code NVARCHAR(20) NOT NULL,
        purpose NVARCHAR(50) NOT NULL,
        expires_at DATETIME2 NOT NULL,
        consumed_at DATETIME2 NULL,
        created_at DATETIME2 NOT NULL,
        CONSTRAINT fk_otp_user FOREIGN KEY (user_id) REFERENCES users(id)
    );
END;

-- Indexes (idempotent)
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name='idx_booking_tourist') CREATE INDEX idx_booking_tourist ON bookings(tourist_id);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name='idx_booking_package') CREATE INDEX idx_booking_package ON bookings(package_id);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name='idx_payment_booking') CREATE INDEX idx_payment_booking ON payments(booking_id);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name='idx_allocation_booking') CREATE INDEX idx_allocation_booking ON allocations(booking_id);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name='idx_ticket_vehicle') CREATE INDEX idx_ticket_vehicle ON maintenance_tickets(vehicle_id);
IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name='idx_notification_user') CREATE INDEX idx_notification_user ON notifications(user_id);
