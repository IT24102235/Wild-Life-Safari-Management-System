-- V6 Refactor OTP table to match current Otp entity model
-- Drops and recreates otps table (fresh dev environment assumed). Adjust if preserving data is required.

IF OBJECT_ID('otps','U') IS NOT NULL
BEGIN
    DROP TABLE otps;
END;

CREATE TABLE otps (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    email NVARCHAR(100) NOT NULL,
    otp_hash NVARCHAR(255) NOT NULL,
    type NVARCHAR(20) NOT NULL,
    attempts INT NOT NULL DEFAULT 0,
    max_attempts INT NOT NULL DEFAULT 5,
    expires_at DATETIME2 NOT NULL,
    used_at DATETIME2 NULL,
    created_at DATETIME2 NOT NULL
);

CREATE INDEX idx_otps_email ON otps(email);
CREATE INDEX idx_otps_type ON otps(type);

