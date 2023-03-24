insert into "course"
    (id, code, name, credits, total_hours, user_id)
values ('course1_id', 'PROG1', 'Algorithmique', 6, 1, 'teacher1_id'),
       ('course2_id', 'PROG3', 'POO avancée', 6, 2, 'teacher2_id'),
       ('course3_id', 'WEB1', 'Interface web', 4, 1, 'teacher1_id');

insert into "have_student"
       (id,id_course,id_user)
values ('course1','course1_id','student2_id'),
        ('course2','course1_id','student3_id'),
        ('course3','course2_id','student3_id');