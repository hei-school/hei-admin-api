insert into "course"
    (id, code, name, credits, total_hours, user_id)
values ('course1_id', 'CRS21001', 'Name of course one', 1, 1, 'teacher1_id'),
       ('course2_id', 'CRS21002', 'Name of course two', 2, 2, 'teacher1_id'),
       ('course3_id', 'PROG1', 'Introduction to Computer Science', 4, 1, 'teacher4_id');

insert into "have_student"
       (id,id_course,id_user)
values ('course1','course1_id','student2_id'),
        ('course2','course1_id','student3_id'),
        ('course3','course2_id','student3_id');