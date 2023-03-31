DO
$$
BEGIN
        IF NOT EXISTS(SELECT FROM pg_type WHERE typname = 'time_rate_type') THEN
CREATE TYPE "time_rate_type" AS ENUM ('DAILY');
END IF;
END
$$;

CREATE TABLE IF NOT EXISTS "delay_penalty" (
                                               id VARCHAR PRIMARY KEY,
                                               interest_percent INTEGER NOT NULL,
                                               interest_timerate time_rate_type NOT NULL,
                                               grace_delay INTEGER NOT NULL,
                                               applicability_delay_after_grace INTEGER NOT NULL,
                                               creation_datetime timestamp with time zone not null
);