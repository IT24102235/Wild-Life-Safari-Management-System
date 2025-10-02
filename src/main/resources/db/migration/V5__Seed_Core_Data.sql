-- V5 Seed Core Reference Data (idempotent)
-- Languages
IF NOT EXISTS (SELECT 1 FROM languages WHERE iso_code = 'en')
    INSERT INTO languages (code, iso_code, name, created_at, updated_at, is_active) VALUES ('EN', 'en', 'English', SYSDATETIME(), SYSDATETIME(), 1);
IF NOT EXISTS (SELECT 1 FROM languages WHERE iso_code = 'es')
    INSERT INTO languages (code, iso_code, name, created_at, updated_at, is_active) VALUES ('ES', 'es', 'Spanish', SYSDATETIME(), SYSDATETIME(), 1);
IF NOT EXISTS (SELECT 1 FROM languages WHERE iso_code = 'fr')
    INSERT INTO languages (code, iso_code, name, created_at, updated_at, is_active) VALUES ('FR', 'fr', 'French', SYSDATETIME(), SYSDATETIME(), 1);
IF NOT EXISTS (SELECT 1 FROM languages WHERE iso_code = 'de')
    INSERT INTO languages (code, iso_code, name, created_at, updated_at, is_active) VALUES ('DE', 'de', 'German', SYSDATETIME(), SYSDATETIME(), 1);

-- Tour Packages
IF NOT EXISTS (SELECT 1 FROM tour_packages WHERE name = 'Sunrise Wild Trail')
    INSERT INTO tour_packages (name, description, days, price, max_people, difficulty_level, included_activities, is_active, created_at, updated_at)
    VALUES ('Sunrise Wild Trail', 'Early morning safari with guided wildlife tracking.', 1, 150.00, 6, 'EASY', 'Jeep safari;Light breakfast', 1, SYSDATETIME(), SYSDATETIME());
IF NOT EXISTS (SELECT 1 FROM tour_packages WHERE name = 'River Edge Explorer')
    INSERT INTO tour_packages (name, description, days, price, max_people, difficulty_level, included_activities, is_active, created_at, updated_at)
    VALUES ('River Edge Explorer', 'Two-day adventure with night camping and river observation.', 2, 420.00, 10, 'MODERATE', 'Jeep safari;Night camp;River watch', 1, SYSDATETIME(), SYSDATETIME());

-- Admin User (optional)
-- NOTE: Password hash corresponds to plaintext 'Admin@123' using BCrypt (cost 10). Change in production.
IF NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin')
BEGIN
    INSERT INTO users (username, email, password_hash, full_name, phone, role, email_verified, enabled, locked, created_at, updated_at)
    VALUES ('admin', 'admin@safari.local', '$2a$10$DowJDmQd6hwz7dBDEuDf8u3tbT8hSdPNgm6kKiC9JIhbSAVnX1J3C', 'System Administrator', '+10000000000', 'ADMIN', 1, 1, 0, SYSDATETIME(), SYSDATETIME());
END;

