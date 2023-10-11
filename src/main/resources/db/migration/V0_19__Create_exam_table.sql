create table if not exists "exam" (
    id varchar constraint exam_pk primary key default uuid_generate_v4(),
    coefficient int not null,
    title varchar not null,
    awarded_course_id varchar not null constraint awarded_course_exam_fk references "awarded_course"(id),
    examination_date timestamp not null
    );
