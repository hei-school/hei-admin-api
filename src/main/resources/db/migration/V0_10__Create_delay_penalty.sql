CREATE TYPE IF NOT EXISTS fee_type AS ENUM ('');

CREATE TABLE IF NOT EXISTS delay_penalty (
    id                       VARCHAR(36) PRIMARY KEY DEFAULT uuid_generate_v4(),
    interest_percent         INTEGER NOT NULL,
    interest_timeframe       ENUM('DAILY', 'WEEKLY', 'MONTHLY') NOT NULL DEFAULT 'DAILY',
    grace_delay              INTEGER NOT NULL,
    applicability_delay_after_grace INTEGER NOT NULL,
    creation_datetime        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS fee_user_id_index ON fee (user_id);
