do
$$
begin
        if not exists(select from pg_type where typname = 'semester') then
            create type semester as enum ('S1', 'S2', 'S3', 'S4', 'S5', 'S6');
        end if;
end
$$;

create table if not exists "transcript" (
    id varchar constraint transcript_pk
        primary key default uuid_generate_v4(),
    semester semester not null,
    academic_year integer check (academic_year > 2020),
    is_definitive boolean not null,
    creation_datetime timestamp,
    student_id varchar not null
        constraint student_fk references "user" (id)
);

create index if not exists student_index on "transcript" (student_id);