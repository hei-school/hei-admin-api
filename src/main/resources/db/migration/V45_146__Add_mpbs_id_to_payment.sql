alter table payment
    add column if not exists mpbs_id varchar constraint payment_mpbs_id_fk references "mpbs" (id);

create unique index if not exists payment_mpbs_id_unique
    on payment (mpbs_id)
    where mpbs_id is not null and is_deleted = false;
