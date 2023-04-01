create table if not exists "course"
(
    id                varchar
    constraint course_pk primary key default uuid_generate_v4(),
    code              varchar              unique not null,
    name              varchar,
    credits           integer,
    total_hours       integer,
    main_teacher_id   varchar
        constraint main_teacher_id_fk references "user"(id)
);
do
&&
begin if not exists (select from pg_type where typname = 'delay_penalty_type') then
    create type "delay_penalty_type" as enum ('DAILY');
     end if;
   end
&&;
create table if not exists 'delay_penalty'
(id varchar
    constraint delay_penalty_pk primary key default uuid_generate_v4(),
    interest_percent integer,
    interest_timerate   delay_penalty_type,
    grace_delay integer,
    applicability_delay_after_grace integer,
    creation_datetime   timestamp with time zone not null default now()
);