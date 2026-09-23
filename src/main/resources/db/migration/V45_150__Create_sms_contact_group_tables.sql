CREATE TABLE IF NOT EXISTS sms_contact_group
(
    id                VARCHAR CONSTRAINT sms_contact_group_pk PRIMARY KEY DEFAULT uuid_generate_v4(),
    name              VARCHAR                  NOT NULL,
    owner_id          VARCHAR REFERENCES "user" (id) NOT NULL,
    is_deleted        BOOLEAN                  NOT NULL DEFAULT FALSE,
    creation_datetime TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS sms_contact_group_member
(
    sms_contact_group_id VARCHAR REFERENCES sms_contact_group (id) NOT NULL,
    sms_contact_id        VARCHAR REFERENCES sms_contact (id) NOT NULL,
    CONSTRAINT sms_contact_group_member_pk PRIMARY KEY (sms_contact_group_id, sms_contact_id)
);
