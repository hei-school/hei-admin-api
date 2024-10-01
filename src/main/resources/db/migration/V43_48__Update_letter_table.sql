alter table "letter" add column if not exists fee_id varchar constraint letter_fee_fk references fee("id");
alter table "letter" add column if not exists amount integer;