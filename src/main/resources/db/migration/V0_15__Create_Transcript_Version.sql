create table if not exists "transcript_version" 
(
    id              varchar 
        constraint transcript_version_pk        primary key default  uuid_generate_v4(),
    transcript_id   varchar 
        constraint transcript_id_fk             references "transcript" (id),
    ref  varchar                  not null
        constraint transcript_version_ref_unique unique,
    createBy        varchar 
        constraint createBy_fk                  references "user" (id),
    creationDatetime timestamp with time zone not null default now()

);
