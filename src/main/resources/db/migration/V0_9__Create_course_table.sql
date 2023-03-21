create extension if not exists "uuid-ossp";

create table if not exists "course"
(
    id                varchar
        constraint course_pk primary key                 default uuid_generate_v4(),
    code              varchar                  not null
        constraint course_code_unique unique,
    name              varchar                  not null,
    credits           integer                  default 0,
    total_hours       integer                  default 0,
    user_id           varchar                  not null
            constraint course_user_id_fk references "user"(id),
    student_id varchar not null
            constraint course_student_id_fk references "user"(id)
);
create table if not exists have_student
(
     id                varchar
            constraint have_student_pk primary key  default uuid_generate_v4()     ,
    id_course    varchar not null references "course" (id),
    id_user varchar not null references "user" (id)
);