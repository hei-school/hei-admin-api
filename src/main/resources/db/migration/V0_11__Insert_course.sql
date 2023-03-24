-- course not linked
insert into "course"
(id, name, code, credits, total_hours, main_teacher_id)
values ('1', 'Algorithms', 'PROG1', 13, 21, 'teacher1'),
       ('2', 'Web interface', 'WEB1', 12, 19, 'teacher2'),
       ('3', 'Arthimetiaue du machine', 'Theorie2', 10, 8, 'teacher1');

-- insert a later linked course
insert into "course"
(id, name, code, credits, total_hours, main_teacher_id)
values