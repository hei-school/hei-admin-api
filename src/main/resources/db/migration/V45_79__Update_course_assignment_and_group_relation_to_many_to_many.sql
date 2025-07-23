create table if not exists "course_assignment_group" (
    id varchar
        constraint course_assignment_group_pk primary key default uuid_generate_v4(),
    "course_assignment_id" varchar
        constraint course_assignment_group_course_assignment_fk references "course_assignment"(id),
    "group_id" varchar
        constraint course_assignment_group_group_fk references "group"(id)
);