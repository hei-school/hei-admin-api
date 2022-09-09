create extension if not exists "uuid-ossp";

create table if not exists "course"
(
    id                varchar
        constraint course_pk primary key                 default uuid_generate_v4(),
    name              varchar                  not null,
    ref               varchar                  not null,
    credits        int                  not null ,
    total_hours        int                  not null
    );