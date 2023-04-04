create table if not exists "student_course" (
    id varchar constraint student_course_pk
    primary key default  uuid_generate_v4(),
    user_id varchar constraint user_id_fk
    references "user" (id),
    course_id varchar constraint course_id_fk
    references "course" (id),
    status course_status not null default 'UNLINKED'
);

create index if not exists user_id_index on "student_course" (user_id);
create index if not exists course_id_index on "student_course" (course_id);