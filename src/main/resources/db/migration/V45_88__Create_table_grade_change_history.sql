create table if not exists "grade_change_history"
(
    id               varchar constraint grade_change_history_pk primary key default uuid_generate_v4(),
    grade_id         varchar references "grade" (id)  not null,
    score            DOUBLE PRECISION                 not null,
    change_instant   timestamp with time zone         default now(),
    comment          text                             not null
);
