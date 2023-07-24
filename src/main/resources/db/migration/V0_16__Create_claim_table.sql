create type claim_status as enum ('OPEN', 'CLOSED');
create table "claim"(
    id varchar primary key,
    reason varchar not null,
    status claim_status not null,
    version_id varchar references "version"(id),
    transcript_id varchar references "transcript"(id),
    creation_datetime timestamp with time zone not null default now(),
    closed_datetime timestamp with time zone
);
