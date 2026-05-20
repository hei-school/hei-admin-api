create type result_overview_status as enum('VALIDATED', 'INVALIDATED', 'IN_PROGRESS', 'NOT_STARTED');
create table if not exists "result_overview"(
    id varchar constraint result_overview_pk primary key default uuid_generated_v4(),
    promotion_id varchar references "promotion" (id) not null,
    student_id varchar references "user"(id) not null,
    weighted_average double precision not null,
    obtained_credits double precision not null,
    status result_overview_status not null,
    total_credits double precision not null
);