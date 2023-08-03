do
$$
    begin
        if not exists(select from pg_type where typname = 'claim_status') then
            create type "claim_status" as enum ('OPEN', 'CLOSE');
        end if;
    end
$$;

create table if not exists "student_transcript_claim"
(
    id                varchar
        constraint student_transcript_claim_pk primary key,
    transcript_id           varchar
        constraint claim_student_transcript_id_fk references "transcript"(id),
    student_transcript_version_id              varchar
        constraint claim_student_transcript_version_id_fk references "student_transcript_version"(id),
    status            claim_status,
    creation_datetime timestamp with time zone,
    closed_datetime timestamp with time zone,
    reason varchar
);
