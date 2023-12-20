do
$$
begin
        if not exists(select from pg_type where typname = 'claim_status') then
create type claim_status as enum ('OPEN', 'CLOSE');
end if;
end
$$;

create table if not exists "transcript_claim" (
    id varchar constraint transcript_claim_pk
        primary key ,
    reason text not null,
    claim_status claim_status not null,
    closed_datetime timestamp with time zone,
    creation_datetime timestamp with time zone not null default now(),
    transcript_version_id varchar not null
        constraint transcript_version_fk references "transcript_version" (id)
);

create index if not exists transcript_version_index on "transcript_claim" (transcript_version_id);