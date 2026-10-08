ALTER TABLE sms_contact_group
    ALTER COLUMN owner_id DROP NOT NULL,
    ADD COLUMN IF NOT EXISTS group_id VARCHAR REFERENCES "group" (id);

CREATE UNIQUE INDEX IF NOT EXISTS sms_contact_group_group_id_uq
    ON sms_contact_group (group_id)
    WHERE group_id IS NOT NULL;
