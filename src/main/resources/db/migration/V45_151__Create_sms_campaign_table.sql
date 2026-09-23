DO
$$
BEGIN
        IF NOT EXISTS (
            SELECT FROM pg_type
            WHERE typname = 'sms_campaign_status'
        ) THEN
CREATE TYPE "sms_campaign_status"
    AS ENUM ('CREATED', 'PENDING', 'DELIVERED', 'FAILED');
END IF;
END
$$;

CREATE TABLE IF NOT EXISTS sms_campaign
(
    id                              VARCHAR CONSTRAINT sms_campaign_pk PRIMARY KEY DEFAULT uuid_generate_v4(),
    message                         VARCHAR,
    status                          sms_campaign_status      NOT NULL DEFAULT 'CREATED',
    failure_reason                  VARCHAR,
    file_import_count               INTEGER                  NOT NULL DEFAULT 0,
    file_bucket_key                 VARCHAR,
    recipient_count                 INTEGER                  NOT NULL DEFAULT 0,
    recipients_rejected_for_balance INTEGER                  NOT NULL DEFAULT 0,
    delivered_count                 INTEGER                  NOT NULL DEFAULT 0,
    failed_count                    INTEGER                  NOT NULL DEFAULT 0,
    sms_segments_each               INTEGER,
    credits_debited                 INTEGER,
    send_at                         TIMESTAMP WITH TIME ZONE,
    created_by_id                   VARCHAR REFERENCES "user" (id) NOT NULL,
    creation_datetime               TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS sms_campaign_status_send_at_idx ON sms_campaign (status, send_at);

CREATE TABLE IF NOT EXISTS sms_campaign_contact_group
(
    campaign_id      VARCHAR REFERENCES sms_campaign (id) NOT NULL,
    contact_group_id VARCHAR REFERENCES sms_contact_group (id) NOT NULL,
    CONSTRAINT sms_campaign_contact_group_pk PRIMARY KEY (campaign_id, contact_group_id)
);

CREATE TABLE IF NOT EXISTS sms_campaign_contact
(
    campaign_id VARCHAR REFERENCES sms_campaign (id) NOT NULL,
    contact_id  VARCHAR REFERENCES sms_contact (id) NOT NULL,
    CONSTRAINT sms_campaign_contact_pk PRIMARY KEY (campaign_id, contact_id)
);

CREATE TABLE IF NOT EXISTS sms_campaign_manual_phone_number
(
    campaign_id  VARCHAR REFERENCES sms_campaign (id) NOT NULL,
    phone_number VARCHAR                              NOT NULL,
    CONSTRAINT sms_campaign_manual_phone_number_pk PRIMARY KEY (campaign_id, phone_number)
);
