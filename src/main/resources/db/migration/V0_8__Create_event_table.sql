create extension if not exists "uuid-ossp";

create table if not exists "event"
(
    id                varchar
    constraint event_pk primary key                 default uuid_generate_v4(),
    event_name             varchar                  not null,
    event_type             varchar                  not null,
    start_date             timestamp with time zone not null default now(),
    end_date               timestamp with time zone not null,
    responsible            varchar                  not null
        constraint responsible_fk references "user"(id),
    place                   varchar                 not null
        constraint place_fk references "place"(id)
);
