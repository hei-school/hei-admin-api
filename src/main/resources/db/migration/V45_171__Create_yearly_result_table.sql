create table if not exists "yearly_result"(
    id varchar constraint yearly_result_pk primary key default uuid_generate_v4(),
    student_id varchar references "user"(id) not null,
    group_id varchar references "group"(id) not null,
    level student_level not null,
    weighted_average double precision,
    obtained_credits double precision,
    total_credits double precision,
    status result_overview_status not null,
    constraint yearly_result_student_level_uq unique (student_id, level)
);
