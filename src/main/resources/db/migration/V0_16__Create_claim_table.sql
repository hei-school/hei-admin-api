create table "claim"(
    id varchar primary key,
    content varchar,
    version_id references "version"(id)
);
