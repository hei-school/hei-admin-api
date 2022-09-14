do
$$
    begin
        if not exists(select from pg_type where typname = 'event_type') then
            create type event_type as enum ('COURSE', 'EXAMINATION', 'MEETING');
        end if;
    end
$$;

create table if not exists "event"
(
    id                varchar
        constraint event_pk primary key default uuid_generate_v4(),
    name              varchar                  not null,
    event_type        event_type               not null,
    starting_time     timestamp with time zone not null,
    ending_time       timestamp with time zone not null,
    supervisor_id     varchar                  not null
        constraint event_supervisor_id_fk references "user"(id),
    place_id          varchar
        constraint event_place_id_fk references "place"(id)
);
