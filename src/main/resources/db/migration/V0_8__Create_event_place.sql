do
$$
    begin
        if not exists(select from pg_type where typname = 'event_type') then
            create type event_type as enum ('COURSES', 'EXAMS', 'CONFERENCES');
        end if;
        if not exists(select from pg_type where typname = 'event_status') then
            create type event_status as enum ('ACTIVE', 'CANCEL');
        end if;
    end
$$;

create table if not exists "event"
(
    id                varchar
        constraint event_pk primary key default uuid_generate_v4(),
    event_type        event_type                not null,
    responsible_id    varchar                   not null
    constraint event_responsible_fk references "user"(id),
    start             timestamp with time zone  not null,
    end_datetime               timestamp with time zone  not null,
    place_id          varchar                   not null
    constraint event_place_fk references "place"(id),
    status            event_status              not null
);
