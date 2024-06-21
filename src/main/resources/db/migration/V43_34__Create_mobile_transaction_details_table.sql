create table if not exists "mobile_transaction_details" (
   id varchar constraint mobile_transaction_details_pk primary key default uuid_generate_v4(),
    psp_transaction_amount integer not null,
    psp_datetime_transaction_creation timestamp,
    psp_transaction_ref varchar not null
);