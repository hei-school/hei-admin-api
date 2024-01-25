-- alter table user
alter table "user" add column if not exists is_deleted boolean default false;

-- alter table payment
alter table "payment" add column if not exists is_deleted boolean default false;

-- alter table fee
alter table "fee" add column if not exists is_deleted boolean default false;

-- alter table group
alter table "group" add column if not exists is_deleted boolean default false;

-- alter table course
alter table "course" add column if not exists is_deleted boolean default false;

-- alter table exam
alter table "exam" add column if not exists is_deleted boolean default false;

-- alter table awarded_course
alter table "awarded_course" add column if not exists is_deleted boolean default false;

-- alter table group_flow
alter table "group_flow" add column if not exists is_deleted boolean default false;

-- alter table attendance
alter table "attendance" add column if not exists is_deleted boolean default false;