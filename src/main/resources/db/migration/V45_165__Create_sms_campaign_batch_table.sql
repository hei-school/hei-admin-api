CREATE TABLE IF NOT EXISTS sms_campaign_batch
(
    id                VARCHAR CONSTRAINT sms_campaign_batch_pk PRIMARY KEY DEFAULT uuid_generate_v4(),
    campaign_id       VARCHAR REFERENCES sms_campaign (id) NOT NULL,
    befiana_batch_id  VARCHAR,
    send_at           TIMESTAMP WITH TIME ZONE NOT NULL,
    recipient_count   INTEGER                  NOT NULL DEFAULT 0,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS sms_campaign_batch_campaign_id_idx ON sms_campaign_batch (campaign_id);

ALTER TABLE sms_log
    RENAME COLUMN callback_data TO befiana_message_id;

ALTER TABLE sms_log
    ADD COLUMN IF NOT EXISTS campaign_batch_id VARCHAR REFERENCES sms_campaign_batch (id);

CREATE INDEX IF NOT EXISTS sms_log_campaign_batch_id_idx ON sms_log (campaign_batch_id);

UPDATE sms_log SET status = 'UNKNOWN', befiana_message_id = NULL WHERE status = 'PENDING';

DROP INDEX IF EXISTS sms_log_status_idx;
CREATE INDEX IF NOT EXISTS sms_log_status_idx ON sms_log (status)
    WHERE status IN ('SCHEDULED', 'PROCESSING', 'ACCEPTED', 'UNKNOWN');
