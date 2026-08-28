alter table payment
    add column if not exists validated_by_id varchar constraint payment_validated_by_id_fk references "user" (id);

alter table credit_transaction
    add column if not exists payment_id varchar constraint credit_transaction_payment_id_fk references "payment" (id);
