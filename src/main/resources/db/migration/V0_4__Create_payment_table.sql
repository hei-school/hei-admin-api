do
$$
    begin
        if not exists(select from pg_type where typname = 'payment_type') then
            create type "payment_type" as enum ('CASH', 'SCHOLARSHIP','FIX');
        end if;
    end
$$;

create table if not exists "payment"
(
    id                varchar
        constraint payment_pk primary key default uuid_generate_v4(),
    fee_id           varchar                  not null
        constraint payment_fee_id_fk references "fee"(id),
    type              payment_type                 not null,
    amount            integer                  not null,
    comment           varchar                  not null,
    creation_datetime timestamp with time zone not null default now()
);
create index if not exists payment_fee_id_index on "payment" (fee_id);
