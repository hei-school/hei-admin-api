do $$
begin
    if not exists (select from pg_type where typname = 'role') then
        create type "role" as enum ('STUDENT','TEACHER','MANAGER');
    end if;
end $$;

create table if not exists "user" (
    id         varchar constraint user_pk primary key,
    first_name varchar not null,
    last_name  varchar not null,
    email      varchar not null constraint email_unique unique,
    role       role not null
);

create index if not exists user_email_index on "user" (email);
create index if not exists user_last_name_index on "user" (last_name);
