insert into "course"
    (id,user_id, code, name, credits, total_hours, main_teacher_id, status)
values ('course1_id','student1_id', 'PROG1', 'Algorithmics', 6, 20, 'teacher1_id', 'UNLINKED'),
       ('course2_id','student1_id', 'WEB1', 'Web interface', 6, 24, 'teacher2_id', 'LINKED'),
       ('course3_id','student1_id', 'SYS1', 'Operating system', 6, 22, 'teacher3_id', 'UNLINKED');