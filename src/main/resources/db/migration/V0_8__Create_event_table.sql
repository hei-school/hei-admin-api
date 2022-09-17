create extension if not exists "uuid-ossp";

create table if not exists "event"
(
    id              varchar
        constraint event_pk primary key default uuid_generate_v4(),
    name            varchar not null,
    ref             varchar not null
        constraint event_ref_unique unique,
    starting_hours  timestamp with time zone,
    ending_hours    timestamp with time zone,
    user_manager_id varchar not null
        constraint event_user_id_fk references "user" (id),
    place_id        varchar not null
        constraint event_place_id_fk references "place" (id)
);
