create table if not exists "remedial" (
                                      id varchar constraint remedial_pk primary key default uuid_generate_v4(),
    title varchar not null,
    course_assignment_id varchar not null constraint course_assignment_remedial_fk references "course_assignment"(id),
    remedial_date timestamp not null
    );
