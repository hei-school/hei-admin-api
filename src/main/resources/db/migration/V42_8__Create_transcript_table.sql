do
$$
    begin
        if not exists(select from pg_type where typname = 'semester_enum') then
            create type "semester_enum" as enum ('S1', 'S2', 'S3', 'S4', 'S5', 'S6');
        end if;
    end
$$;

create table if not exists "transcript"
(
    id                varchar
        constraint transcript_pk primary key,
    user_id           varchar                  not null
        constraint transcript_user_id_fk references "user" (id),
    semester          semester_enum            not null,
    academic_year     integer                  not null,
    is_definitive     boolean                  not null default false,
    creation_datetime timestamp with time zone not null default now()
);
create index if not exists transcript_user_id_index on "transcript" (user_id);

