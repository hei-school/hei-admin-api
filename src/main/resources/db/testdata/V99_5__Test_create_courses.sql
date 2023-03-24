-- course not linked
insert into "course"
(id, name, code, credits, total_hours, main_teacher_id)
values ('course1_id', 'Algorithms', 'PROG1', 12, 22, 'teacher1_id'),
       ('course2_id', 'Web interface locally interactive', 'WEB1', 12, 20, 'teacher2_id');

-- insert a later linked course
insert into "course"
(id, name, code, credits, total_hours, main_teacher_id)
values ('course3_id', 'UI/UX', 'WEB2', 10, 20, 'teacher2_id');

-- precisely an updateStudentCourse
update "course" set status='LINKED' where id='course3_id';

-- course linked
insert into "course_student"
(id, course_id, student_id)
values (1, 'course3_id', 'student1_id');



