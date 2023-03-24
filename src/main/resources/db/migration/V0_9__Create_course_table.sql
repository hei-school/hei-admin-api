do
$$
    begin
        if not exists(select from pg_type where typname = 'course_status') then
            create type "course_status" as enum ('LINKED', 'UNLINKED');
        end if;
    end
$$;

create table if not exists "course"
(
    id                varchar
        constraint course_pk primary key default uuid_generate_v4(),
    main_teacher_id           varchar                  not null
        constraint user_id_fk references "user"(id),
    name              varchar                  ,
    code              varchar                  ,
    total_hours       integer                  ,
    credits           integer                  ,
    status            course_status not null default 'LINKED'
);

create table if not exists "course_student"
(
    id integer
        constraint course_student_pk primary key,
    course_id varchar not null
        constraint course_fk references "course"(id),
    student_id varchar not null
        constraint user_id_fk references "user"(id)
);


create index if not exists course_name_index on "course" (name);
