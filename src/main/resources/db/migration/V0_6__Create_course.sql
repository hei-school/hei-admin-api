create extension if not exists "uuid-ossp";

create table if not exists "course"
(
    id                varchar
        constraint course_pk primary key                 default uuid_generate_v4(),
    ref               varchar                  not null,
    name              varchar                  not null
        constraint course_ref_unique unique,
    credits  int  not null,
    total_hours int not null
);
