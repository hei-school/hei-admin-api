create extension if not exists "uuid-ossp";

do
$$
    begin
        if not exists(select from pg_type where typname = 'event_participant_status_type') then
            create type "event_participant_status_type" as enum ('EXPECTED', 'HERE','MISSING');
        end if;
    end
$$;

create table if not exists "event_participant"
(
    id               varchar
        constraint event_participant_pk primary key default uuid_generate_v4(),
    status           event_participant_status_type not null,
    user_participant_id varchar                       not null
        constraint event_participant_user_id_fk references "user" (id),
    event_id            varchar                       not null
        constraint event_participant_event_id_fk references "event" (id)
);
