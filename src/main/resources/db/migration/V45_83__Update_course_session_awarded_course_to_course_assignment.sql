alter table course_session
drop constraint course_session_awareded_course_fk;

alter table course_session
    rename column awarded_course_id to course_assignment_id;

alter table course_session
    add constraint course_session_course_assignment_fk
        foreign key (course_assignment_id)
            references course_assignment(id)
            on delete restrict;
