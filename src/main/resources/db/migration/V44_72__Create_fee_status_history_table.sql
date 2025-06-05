CREATE TABLE IF NOT EXISTS "fee_status_history" (
    id       varchar           NOT NULL CONSTRAINT fee_status_history_pk PRIMARY KEY default uuid_generate_v4(),
    datetime timestamp         NOT NULL default current_timestamp,
    status   fee_status        NOT NULL DEFAULT 'UNPAID'::fee_status,
    id_fee   character varying NOT NULL constraint fee_status_fk references "fee" (id)
);

