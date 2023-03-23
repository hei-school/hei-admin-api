do
$$
begin
        if
not exists(select from pg_type where typname = 'fee_status') then
create type "fee_status" as enum ('UNPAID', 'PAID', 'LATE');
end if;
end
$$;

alter table "fee"
    add column if not exists status fee_status default 'UNPAID';
create index if not exists fee_status_index on "fee" (status);