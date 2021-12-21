create table if not exists "group" (
    id                varchar constraint group_pk primary key default uuid_generate_v4(),
    name              varchar not null,
    ref               varchar not null,
    creation_datetime timestamp not null default now()
);
