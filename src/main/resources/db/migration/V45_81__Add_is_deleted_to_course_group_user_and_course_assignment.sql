alter table "course" add column is_deleted boolean default false;
alter table "user" add column is_deleted boolean default false;
alter table "group" add column is_deleted boolean default false;
alter table "course_assignment" add column is_deleted boolean default false;
