CREATE TABLE IF NOT EXISTS student_badge
(
    id                  VARCHAR CONSTRAINT student_badge_pk PRIMARY KEY DEFAULT uuid_generate_v4(),
    student_id          VARCHAR REFERENCES "user" (id) NOT NULL,
    public_id           VARCHAR                  NOT NULL UNIQUE,
    creation_datetime   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    revocation_datetime TIMESTAMP WITH TIME ZONE
);

CREATE UNIQUE INDEX IF NOT EXISTS student_badge_active_student_idx
    ON student_badge (student_id) WHERE revocation_datetime IS NULL;
