create table "transcript"(
    id varchar primary key,
    student_id references "user"(id)
);
