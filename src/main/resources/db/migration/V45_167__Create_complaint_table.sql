DO
$$
    BEGIN
        IF NOT EXISTS(SELECT FROM pg_type WHERE typname = 'complaint_status') THEN
            CREATE TYPE "complaint_status" AS ENUM ('CREATED', 'IN_PROGRESS', 'RESOLVED', 'REJECTED');
        END IF;
    END
$$;

CREATE TABLE IF NOT EXISTS complaint
(
    id                 VARCHAR CONSTRAINT complaint_pk PRIMARY KEY        DEFAULT uuid_generate_v4(),
    type               complaint_type                                    NOT NULL,
    title              VARCHAR,
    description        VARCHAR,
    status             complaint_status                                  NOT NULL DEFAULT 'CREATED',
    student_id         VARCHAR REFERENCES "user" (id)                    NOT NULL,
    created_at         TIMESTAMP WITH TIME ZONE DEFAULT now(),
    reminder_sent_at   TIMESTAMP WITH TIME ZONE
);
