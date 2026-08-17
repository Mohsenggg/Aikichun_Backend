-- Add email_verified and account_status to users table
ALTER TABLE users
    ADD COLUMN email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN account_status VARCHAR(20) NOT NULL DEFAULT 'PENDING_VERIFICATION';

ALTER TABLE users
    ADD CONSTRAINT chk_account_status
        CHECK (account_status IN ('PENDING_VERIFICATION', 'ACTIVE', 'SUSPENDED', 'BANNED'));

-- Create otps table
CREATE TABLE otps (
                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                      email VARCHAR(255) NOT NULL,
                      otp_code VARCHAR(255) NOT NULL,
                      expires_at TIMESTAMP NOT NULL,
                      consumed BOOLEAN NOT NULL DEFAULT FALSE,
                      created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_otps_email ON otps(email);
CREATE INDEX idx_otps_expires_at ON otps(expires_at);
