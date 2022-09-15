create extension if not exists "uuid-ossp";

create table if not exists "place"
(
    id                varchar
    constraint place_pk primary key                 default uuid_generate_v4(),
    name              varchar                  not null
);
