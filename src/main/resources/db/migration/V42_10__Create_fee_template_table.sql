create table if not exists predefined_fee_type(
    id varchar
    constraint predefined_fee_type_key primary key default uuid_generate_v4(),
    name varchar not null,
    creation_datetime timestamp with time zone not null default now(),
    total_amount int not null,
    number_of_months int not null
);