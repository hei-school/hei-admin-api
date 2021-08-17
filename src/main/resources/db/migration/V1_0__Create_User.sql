--------------
---- User ----
--------------

create table if not exists users (
    id          serial constraint user_pk primary key,
    first_name   varchar(50) not null,
    last_name    varchar(50) not null,
    email       varchar(50) not null constraint email_unique unique,
    role        varchar(50) not null
);