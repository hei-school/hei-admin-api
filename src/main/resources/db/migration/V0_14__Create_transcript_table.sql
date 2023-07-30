do
$$
    begin
        if not exists(select from pg_type where typname = 'semester') then
            create type "semester" as enum ('S1', 'S2', 'S3', 'S4', 'S5', 'S6');
        end if;
    end
$$;

create table if not exists "transcript"
(
    id                varchar
        constraint transcript_pk primary key default uuid_generate_v4(),
    user_id           varchar                  not null
        constraint transcript_user_id_fk references "user"(id),
    semester          semester                 not null,
    academic_year     integer                  not null,
    is_definitive     boolean                  not null,
    creation_datetime timestamp with time zone not null default now()
);
create index if not exists transcript_user_id_index on "transcript" (user_id);
