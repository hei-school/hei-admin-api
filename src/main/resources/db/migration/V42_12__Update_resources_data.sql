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