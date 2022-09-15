do
$$
    begin
        if not exists(select from pg_type where typname = 'status') then
            create type status as enum ('EXPECTED', 'HERE', 'MISSING');
        end if;
    end
$$;

create table if not exists "event_participant"
(
    id                varchar
        constraint event_participant_pk primary key default uuid_generate_v4(),
    event_id              varchar                  not null
        constraint event_participant_event_id_fk references "event"(id),
    participant_id        varchar                  not null
        constraint event_participant_participant_id_fk references "user"(id),
    status                status                   not null
);
