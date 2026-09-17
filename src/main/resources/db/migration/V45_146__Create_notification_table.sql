DO
$$
BEGIN
        IF NOT EXISTS (
            SELECT FROM pg_type
            WHERE typname = 'notification_resolution_status'
        ) THEN
CREATE TYPE "notification_resolution_status"
    AS ENUM ('UNRESOLVED', 'IN_PROGRESS', 'RESOLVED');
END IF;
END
$$;

CREATE TABLE IF NOT EXISTS notification
(
    id                  VARCHAR CONSTRAINT notification_pk PRIMARY KEY DEFAULT uuid_generate_v4(),
    recipient_id        VARCHAR REFERENCES "user" (id) NOT NULL,
    subject             VARCHAR                  NOT NULL,
    body                VARCHAR                  NOT NULL,
    "read"              BOOLEAN                  NOT NULL DEFAULT FALSE,
    resolution_status   notification_resolution_status,
    sms_campaign_id     VARCHAR REFERENCES sms_campaign (id),
    creation_datetime   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS notification_recipient_id_idx ON notification (recipient_id);
