create table if not exists "event"
(
    id                varchar
        constraint event_pk primary key default uuid_generate_v4(),
    "type"           varchar                  not null,
    responsible           varchar                  not null
        constraint event_fk_user_id references "user"(id),
    place              varchar                 not null
        constraint event_fk_place_id references "place"(id),
    start_datetime              timestamp with time zone       not null,
    end_datetime              timestamp with time zone                 not null
);