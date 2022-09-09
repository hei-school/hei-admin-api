create extension if not exists "uuid-ossp";

create table if not exists "course"
(
    id                varchar
        constraint course_pk primary key                 default uuid_generate_v4(),
    ref              varchar                  not null
        constraint course_ref_unique unique,
    name             varchar                  not null,
    credits          float                    not null,
    total_hours      float                    not null
);
