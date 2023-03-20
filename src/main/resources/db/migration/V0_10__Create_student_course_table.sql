do
$$
    begin
        if not exists(select from pg_type where typname = 'course_status') then
            create type "course_status" as enum ('LINKED', 'UNLINKED');
        end if;
    end
$$;
create table if not exists "student_course"
(
    id                varchar
        constraint course_student_pk primary key default uuid_generate_v4(),
    course_id              varchar                  not null
        constraint student_course_course_id_fk references "course"(id),
    student_id        varchar                  not null
        constraint student_course_student_id_fk references "user"(id),
    status              course_status                     not null
);
