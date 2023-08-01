create table if not exists "student_transcript_version"
(
    id                varchar
        constraint student_transcript_version_pk primary key,
    transcript_id           varchar
        constraint student_transcript_id_fk references "transcript"(id),
    ref              int           ,
    responsible_id            varchar
        constraint responsible_version_id_fk references "user"(id),
    creation_datetime timestamp with time zone
);
