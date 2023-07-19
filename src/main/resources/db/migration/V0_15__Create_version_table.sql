create table "version"(
    id varchar primary key,
    description varchar,
    pdf_url varchar,
    transcript_id references "transcript"(id)
);
