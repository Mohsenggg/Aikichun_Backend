CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       full_name VARCHAR(255) NOT NULL,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       date_of_birth DATE NOT NULL,
                       profession VARCHAR(255),
                       branch VARCHAR(50) NOT NULL,
                       phone_number VARCHAR(50) NOT NULL,
                       phone_number_emergency VARCHAR(50) NOT NULL,
                       services_or_experience TEXT,
                       products_and_discounts TEXT,
                       martial_arts_experience TEXT,
                       has_chronic_condition_or_injury BOOLEAN NOT NULL DEFAULT FALSE,
                       medical_notes TEXT,
                       subscription_start_month INTEGER NOT NULL,
                       subscription_start_year INTEGER NOT NULL,
                       profile_photo_path VARCHAR(500),
                       id_photo_path VARCHAR(500),
                       role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
                       created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                       updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
                       CHECK (subscription_start_month BETWEEN 1 AND 12),
                       CHECK (subscription_start_year >= 2000),
                       CHECK (branch IN ('SIX_OF_OCTOBER', 'N90_TAGAMO3', 'TAHRIR_DOWNTOWN')),
                       CHECK (role IN ('ADMIN', 'MEMBER'))
);

CREATE INDEX idx_users_email ON users(email);
