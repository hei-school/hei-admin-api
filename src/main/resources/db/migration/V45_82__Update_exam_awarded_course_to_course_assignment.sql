alter table exam
drop constraint awarded_course_exam_fk;

alter table exam
    rename column awarded_course_id to course_assignment_id;

alter table exam
    add constraint course_assignment_exam_fk
        foreign key (course_assignment_id)
            references course_assignment(id)
            on delete restrict;