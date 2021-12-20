create table "group" (
    id                varchar constraint group_pk primary key,
    name              varchar not null,
    ref               varchar not null,
    creation_datetime timestamp not null default now()
);
