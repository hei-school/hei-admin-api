DO
$$
BEGIN
        IF NOT EXISTS (
            SELECT FROM pg_type
            WHERE typname = 'sms_contact_owner_role'
        ) THEN
CREATE TYPE "sms_contact_owner_role"
    AS ENUM ('ADMIN', 'MONITOR', 'STUDENT', 'MANAGER');
END IF;
END
$$;

CREATE TABLE IF NOT EXISTS sms_contact
(
    id                VARCHAR CONSTRAINT sms_contact_pk PRIMARY KEY DEFAULT uuid_generate_v4(),
    phone_number      VARCHAR                  NOT NULL,
    name              VARCHAR                  NOT NULL,
    owner_id          VARCHAR REFERENCES "user" (id) NOT NULL,
    owner_role        sms_contact_owner_role   NOT NULL,
    is_deleted        BOOLEAN                  NOT NULL DEFAULT FALSE,
    creation_datetime TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT sms_contact_owner_unique UNIQUE (owner_id)
);

CREATE INDEX IF NOT EXISTS sms_contact_phone_number_idx ON sms_contact (phone_number);
