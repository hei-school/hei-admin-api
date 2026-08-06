do
$$
    begin
        if not exists (
            select
            from pg_type
            where typname = 'credit_movement'
        ) then
            create type credit_movement as enum (
                'WITHDRAWAL',
                'DEPOSIT'
                );
        end if;
    end;
$$;

create table credit_transaction (
    id varchar primary key default uuid_generate_v4(),
    credit_id varchar not null constraint credit_fkey references "credit"(id),
    fee_id varchar not null constraint fee_fkey references "fee"(id),
    credit_movement credit_movement not null,
    amount integer not null,
    creation_datetime timestamp with time zone not null default now()
);