create table if not exists "place"
(
    id                varchar
        constraint place_pk primary key default uuid_generate_v4(),
    location           varchar                  not null,
    city              varchar                 not null
);
create index if not exists place_location_index on "place" (location);
