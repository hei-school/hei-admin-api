do
$$
    begin
        if not exiists(select from pg_type where typename="student_status") then
            create type student_status as enum("LINKED" , "UNLINKED");
        end if;
    end
$$;

create extension if not exists "uuid-ossp";

create table if not exists "student"
(
    id                varchar
        constraint student_pk primary key                 default uuid_generate_v4(),
    student_id   varchar     not null
        constraint student_id_fk references "user"(id),
    course_id   varchar     not null
    constraint course_id_fk references "course"(id),
    status
);
