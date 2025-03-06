create table if not exists "payment_number_sequence" (
    id varchar constraint payment_number_sequence_pk primary key default uuid_generate_v4(),
    date_part varchar not null,
    sequence_number integer not null,
    constraint uq_payment_number_sequence unique (date_part, sequence_number)
)