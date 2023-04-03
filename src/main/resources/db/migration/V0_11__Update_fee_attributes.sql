alter table "fee"
    add column if not exists interest_amount integer default 0;
alter table "fee"
    add column if not exists remaining_amount_with_interest integer;
alter table "fee"
    add column if not exists total_amount_with_interest integer;