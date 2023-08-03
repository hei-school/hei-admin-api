do
$$
begin
        if not exists(select from pg_type where typname = 'semester') then
create type "semester" as enum ('S1', 'S2', 'S3', 'S4', 'S5', 'S6');
end if;
end
$$;


create table if not exists "transcript" (
    id varchar constraint transcript_pk
    primary key default uuid_generate_v4(),
    student_id varchar not null
    constraint transcript_user_id_fk references "user"(id),
    semester semester,
    academic_year       integer check( academic_year > 0),
    is_defenitive boolean,
    creation_datetime timestamp with time zone not null default now()
)