alter table "fee_template" add column category fee_category not null default 'UNKNOWN'::fee_category;
alter table "fee_template" add column frequency fee_frequency not null default 'UNKNOWN'::fee_frequency;
