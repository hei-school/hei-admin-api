do
$$
    begin
        if not exists(select from pg_type where typname = 'semester_type') then
            create type "semester_type" as enum ('S1', 'S2', 'S3', 'S4', 'S5', 'S6');
        end if;
    end
$$;

create table if not exists "transcript"
(
    id                varchar
        constraint transcript_pk primary key default uuid_generate_v4(),
    semester          semester_type            not null,
    student_id        varchar                  not null
        constraint transcript_student_id_fk references "user"(id),
    academic_year     integer                  not null,
    is_definitive     boolean                  not null,
    creation_datetime timestamp with time zone not null default now()
);