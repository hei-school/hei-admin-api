do
$$
    begin
        if not exists(select from pg_type where typname = 'semester_enum') then
            create type "semester_enum" as enum ('S1', 'S2','S3','S4', 'S5','S6');
        end if;
    end
$$;

create table if not exists "transcript"
(
    id                varchar
        constraint transcript_pk primary key,
    student_id           varchar
        constraint transcript_student_id_fk references "user"(id),
    semester              semester_enum           ,
    academic_year            integer             ,
    is_definitive           boolean            ,
    creation_datetime timestamp with time zone
);
create index if not exists transcript_student_id_index on "transcript" (student_id);
