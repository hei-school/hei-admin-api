alter table fee
    add column if not exists archive_requested_datetime timestamp with time zone,
    add column if not exists archived_datetime timestamp with time zone,
    add column if not exists rejected_by_id varchar constraint fee_rejected_by_id_fk references "user" (id),
    add column if not exists rejected_datetime timestamp with time zone,
    add column if not exists rejection_reason varchar;
