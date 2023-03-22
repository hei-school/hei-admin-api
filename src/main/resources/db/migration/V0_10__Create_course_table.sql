create table if not exists "course" (
    id                varchar
         constraint course_pk primary key default uuid_generate_v4(),
    code varchar unique not null,
    name varchar not null,
    credits int check ( credits > 0 ),
    total_hours int check( total_hours > 0),
    main_teacher varchar constraint user_id_fk references "user"(id)
);