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
        primary key default uuid_generate_v4(),
    reason text not null,
    claim_status claim_status not null,
    closed_datetime timestamp,
    creation_datetime timestamp,
    transcript_version_id varchar not null
        constraint transcript_version_fk references "transcript_version" (id)
);

create index if not exists transcript_version_index on "transcript_claim" (transcript_version_id);