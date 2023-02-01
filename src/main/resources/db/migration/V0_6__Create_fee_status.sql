do
$$
    begin
        if not exists(select from pg_type where typname = 'fee_status') then
            create type "fee_status" as enum ('UNPAID', 'PAID', 'LATE');
        end if;
    end
$$;