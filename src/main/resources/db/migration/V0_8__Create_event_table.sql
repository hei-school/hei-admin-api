do
$$
    begin
        if not exists(select from pg_type where typname = 'event_status') then
            create type event_status as enum ('ONGOING', 'CANCELLED');
        end if;
        if not exists(select from pg_type where typname = 'event_type') then
            create type event_type as enum ('COURSE', 'EXAM', 'ENTRANCE_TEST', 'MEETING');
        end if;
    end
$$;

create table if not exists "event"
(
    id                varchar
        constraint event_pk primary key default uuid_generate_v4(),
    name              varchar                  not null,
    user_id           varchar                  not null
    constraint event_user_id_fk references "user"(id),
    place_id           varchar                  not null
    constraint event_place_id_fk references "place"(id),
    type                event_type              not null,
    status            event_status             not null,
    start_event             timestamp with time zone not null default now(),
    end_event               timestamp with time zone not null
);
create index if not exists event_user_id_index on "event" (user_id);