do
$$
    begin
        if not exists(select from pg_type where typname = 'course_student_status') then
            create type "course_student_status" as enum ('UNLINKED', 'LINKED');
        end if;
    end
$$;
create table if not exists "course_student"
(
     id                  varchar
            constraint course_student_pk primary key default uuid_generate_v4(),
     student_id          varchar                     not null
            constraint student_id_fk references "user"(id),
     status              course_student_status       not null
);