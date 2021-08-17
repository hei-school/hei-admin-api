--------------
---- User ----
--------------
drop type if exists "role";
create type "role" as enum ('STUDENT','TEACHER','MANAGER');

drop table if exists users;
drop table if exists "user";
create table "user" (
    id          varchar constraint user_pk primary key,
    first_name   varchar not null,
    last_name    varchar not null,
    email       varchar not null constraint email_unique unique,
    role        role not null
);

create index if not exists email_index on "user" (email);
create index if not exists last_name_index on "user" (last_name);