alter table payment
    add column if not exists rejected_by_id varchar constraint payment_rejected_by_id_fk references "user" (id),
    add column if not exists rejected_datetime timestamp with time zone,
    add column if not exists rejection_reason varchar;
