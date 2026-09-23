DO
$$
BEGIN
        IF NOT EXISTS (
            SELECT FROM pg_type
            WHERE typname = 'sms_message_status'
        ) THEN
CREATE TYPE "sms_message_status"
    AS ENUM ('PENDING', 'DELIVERED', 'FAILED');
END IF;
        IF NOT EXISTS (
            SELECT FROM pg_type
            WHERE typname = 'sms_recipient_source'
        ) THEN
CREATE TYPE "sms_recipient_source"
    AS ENUM ('CONTACT_GROUP', 'MANUAL_SELECTION', 'MANUAL_NUMBER', 'IMPORTED_FILE');
END IF;
END
$$;

CREATE TABLE IF NOT EXISTS sms_log
(
    id                  VARCHAR CONSTRAINT sms_log_pk PRIMARY KEY DEFAULT uuid_generate_v4(),
    campaign_id         VARCHAR REFERENCES sms_campaign (id) NOT NULL,
    phone_number        VARCHAR                  NOT NULL,
    status              sms_message_status,
    recipient_source    sms_recipient_source      NOT NULL,
    contact_id          VARCHAR REFERENCES sms_contact (id),
    sent_datetime       TIMESTAMP WITH TIME ZONE,
    delivered_datetime  TIMESTAMP WITH TIME ZONE,
    callback_data       VARCHAR,
    personalized_message VARCHAR
);

CREATE INDEX IF NOT EXISTS sms_log_campaign_id_idx ON sms_log (campaign_id);
CREATE INDEX IF NOT EXISTS sms_log_status_idx ON sms_log (status) WHERE status = 'PENDING';
