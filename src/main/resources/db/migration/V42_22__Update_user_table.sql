alter table "user" add column if not exists is_working_study boolean default false;
alter table "user" add column if not exists taken_working_study default false;