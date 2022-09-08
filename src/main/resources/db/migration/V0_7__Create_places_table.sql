create table if not exists "place"
(
    id                varchar
        constraint place_pk primary key default uuid_generate_v4(),
    room              varchar                  not null,
    location          varchar                  not null
);
