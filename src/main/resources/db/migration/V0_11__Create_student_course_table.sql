create table if not exists "student_course" (
     id varchar primary key constraint student_course_pk default uuid_generate_v4() ,
     student_id varchar constraint user_id_fk references "user" (id),
     course_id varchar constraint course_id_fk references "course" (id),
     status course_status not null default 'UNLINKED'
);