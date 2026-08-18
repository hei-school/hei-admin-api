alter table fee
    add column if not exists archived_by_id varchar constraint fee_archived_by_id_fk references "user" (id);
