-- alter table payment
alter table "payment" add column if not exists is_deleted boolean default false;

-- alter table fee
alter table "fee" add column if not exists is_deleted boolean default false;