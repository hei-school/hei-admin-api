create table if not exists "course"
(
    id                varchar
        constraint fee_pk primary key default uuid_generate_v4(),
    code              varchar                  not null,
    name              varchar                  not null,
    credit            integer                      not null,
    main_teacher      integer not null 
        constraint main_teacher_pk references "user" (id),
)