-- Create table for Penalty instances
CREATE TABLE IF NOT EXISTS penalty (
                         id VARCHAR PRIMARY KEY,
                         interest_percent INTEGER NOT NULL,
                         interest_time_rate TEXT NOT NULL,
                         grace_delay INTEGER NOT NULL,
                         applicability_delay_after_grace INTEGER NOT NULL,
                         creation_date_time TIMESTAMP WITHOUT TIME ZONE NOT NULL
);
