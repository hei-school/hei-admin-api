do
$$
    BEGIN
        if not exists(select from pg_type where typname = 'event_participant_status') then
            create type "event_participant_status" as enum ('EXPECTED', 'HERE', 'MISSING');
        end if;
    end
$$;

CREATE TABLE event_participant
(
    id                       VARCHAR(255) NOT NULL,
    user_id                  VARCHAR(255),
    status event_participant_status not null ,
    event_id                 VARCHAR(255),
    CONSTRAINT pk_event_participant PRIMARY KEY (id)
);

ALTER TABLE event_participant
    ADD CONSTRAINT FK_EVENT_PARTICIPANT_ON_EVENT FOREIGN KEY (event_id) REFERENCES "event" (id);

ALTER TABLE event_participant
    ADD CONSTRAINT FK_EVENT_PARTICIPANT_ON_USER FOREIGN KEY (user_id) REFERENCES "user" (id);