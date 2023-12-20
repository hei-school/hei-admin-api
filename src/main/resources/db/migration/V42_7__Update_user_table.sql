alter table "user" add column if not exists birth_place varchar default '';
alter table "user" add column if not exists nic varchar default '';
alter table "user" add column if not exists academic_stream academic_stream default null;