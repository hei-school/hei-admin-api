create table if not exists "awarded_course" (
    id varchar constraint awarded_course_pk primary key default uuid_generate_v4(),
    teacher_id varchar not null constraint awarded_course_user_id_fk references "user"(id),
    course_id varchar not null constraint awarded_course_course_id_fk references "course"(id),
    group_id varchar not null constraint awarded_course_group_id_fk references "group"(id)
    );