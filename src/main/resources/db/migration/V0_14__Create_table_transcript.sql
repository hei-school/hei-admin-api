create table if not exists "transcript"
(
    id              varchar
        constraint transcript_pk        primary key default  uuid_generate_v4(),
    student_id      varchar
        constraint student_id_fk             references "user" (id),
    semester        varchar,
    academicYear    varchar,
    isDefinitive    varchar,
    creationDatetime timestamp with time zone not null default now()

);
