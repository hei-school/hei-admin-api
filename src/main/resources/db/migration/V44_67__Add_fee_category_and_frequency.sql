do
$$
    begin
        if not exists(select from pg_type where typname = 'fee_category') then
            create type "fee_category" as enum ('L1','L2','L3','OTHER', 'WORK_FEES', 'UNKNOWN', 'EMPTY_GF');
        end if;

        if not exists(select from pg_type where typname = 'fee_frequency') then
            create type "fee_frequency" as enum ('YEARLY', 'MONTHLY', 'UNKNOWN');
        end if;
    end
$$;

alter table "fee"
    add column category fee_category not null default 'UNKNOWN'::fee_category;

alter table "fee"
    add column frequency fee_frequency not null default 'UNKNOWN'::fee_frequency;
