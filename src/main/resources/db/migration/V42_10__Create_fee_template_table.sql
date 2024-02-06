create table if not exists fee_template(
    id varchar
    constraint fee_template_key primary key default uuid_generate_v4(),
    name varchar not null,
    type fee_type not null,
    creation_datetime timestamp with time zone not null default now(),
    amount int not null,
    number_of_payments int not null
);