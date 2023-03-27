create extension if not exists "uuid-ossp";

create table if not exists "group"
(
    id                varchar
        constraint group_pk primary key                 default uuid_generate_v4(),
    name              varchar                  not null,
    ref               varchar                  not null
        constraint group_ref_unique unique,
    creation_datetime timestamp with time zone not null default now()
);
