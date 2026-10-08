alter table credit_transaction
    add column if not exists balance integer not null default 0;
