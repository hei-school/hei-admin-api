alter table "fee" add column if not exists status fee_status default 'UNPAID';
create index if not exists fee_status_index on "fee" (status);
alter table "fee" add column if not exists remaining_amount integer;