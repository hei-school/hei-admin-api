alter table credit_transaction
    add column if not exists "type" varchar(20);

update credit_transaction
set type = 'CREDIT_PAYMENT'
where credit_movement = 'DEBIT'
  and type is null;

update credit_transaction ct
set type = 'FEE_ARCHIVING'
from fee f
where ct.fee_id = f.id
  and ct.credit_movement = 'CREDIT'
  and f.is_archived = true
  and ct.type is null;

update credit_transaction
set type = 'FEE_OVERPAYMENT'
where credit_movement = 'CREDIT'
  and type is null;

alter table credit_transaction
    alter column "type" set not null;
