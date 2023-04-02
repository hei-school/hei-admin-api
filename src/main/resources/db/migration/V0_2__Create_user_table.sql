do
$$
    begin
        if not exists(select from pg_type where typname = 'role') then
            create type "role" as enum ('STUDENT', 'TEACHER', 'MANAGER');
        end if;
        if not exists(select from pg_type where typname = 'user_status') then
            create type user_status as enum ('ENABLED', 'DISABLED');
        end if;
        if not exists(select from pg_type where typname = 'sex') then
            create type sex as enum ('M', 'F');
        end if;
    end
$$;

create table if not exists "user"
(
    id                varchar
        constraint user_pk primary key default uuid_generate_v4(),
    first_name        varchar                  not null,
    last_name         varchar                  not null,
    email             varchar                  not null
        constraint user_email_unique unique,
    ref               varchar                  not null
        constraint user_ref_unique unique,
    status            user_status              not null,
    sex               sex                      not null,
    birth_date        date                     not null,
    entrance_datetime timestamp with time zone not null,
    phone             varchar                  not null,
    address           varchar                  not null,
    role              role                     not null
);
create index if not exists user_last_name_index on "user" (last_name);
