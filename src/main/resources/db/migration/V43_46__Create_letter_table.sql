do
$$
begin
    if not exists(select from pg_type where typname = 'letter_status') then
create type "letter_status" as enum ('PENDING', 'RECEIVED');
end if;
end
$$;

create table if not exists letter(
    id varchar constraint letter_pk primary key default uuid_generate_v4(),
    description varchar not null,
    creation_datetime timestamp with time zone not null default now(),
    approval_datetime timestamp with time zone,
    student_id varchar constraint letter_fk references "user"(id),
    file_path varchar not null,
    status letter_status not null,
    "ref" varchar unique not null
)