alter table "letter" add column if not exists fee_id varchar;
alter table "letter" add constraint lt_fee_fk foreign key (fee_id) references "fee"("id");
alter table "letter" add column if not exists amount integer;