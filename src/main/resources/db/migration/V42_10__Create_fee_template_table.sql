do
$$
    begin
        if not exists(select from pg_type where typname = 'fee_template_type') then
        create type "fee_template_type" as enum ('TUITION', 'HARDWARE', 'STUDENT_INSURANCE', 'REMEDIAL_COSTS');
    end if;
 end
$$;

create table if not exists fee_template(
    id varchar
    constraint fee_template_key primary key default uuid_generate_v4(),
    name varchar not null,
    type fee_template_type not null,
    creation_datetime timestamp with time zone not null default now(),
    amount int not null,
    number_of_payments int not null
);