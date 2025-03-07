create table if not exists "payment_number_sequence" (
    id varchar constraint payment_number_sequence_pk primary key default uuid_generate_v4(),
    year_month varchar not null,
    sequence_number integer not null,
    constraint uq_payment_number_sequence unique (year_month, sequence_number)
)