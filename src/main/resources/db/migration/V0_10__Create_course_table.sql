create table if not exists "course" (
    id varchar constraint course_pk
    primary key default uuid_generate_v4(),
    code varchar unique not null,
    name varchar not null,
    credits integer not null,
    total_hours integer check ( total_hours > 0 ),
    main_teacher varchar not null
    constraint user_fk references "user" (id)
);

create index if not exists main_teacher_index on "course" (main_teacher);