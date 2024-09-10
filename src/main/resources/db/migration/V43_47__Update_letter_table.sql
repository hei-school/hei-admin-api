alter type "letter_status" add value 'REJECTED';
alter table "letter" add column if not exists reason_for_refusal varchar(250);