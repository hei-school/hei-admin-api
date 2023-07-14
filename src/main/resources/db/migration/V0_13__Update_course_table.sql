alter table course
    alter column code drop not null;
alter table course
    alter column name drop not null;
alter table course
    alter column credits drop not null;
alter table course drop constraint "course_total_hours_check";
alter table course
    alter column main_teacher drop not null;