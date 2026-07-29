do
$$
    begin
        if exists(select from pg_type where typname = 'payment_type') then
            alter type "payment_type" add value 'CREDIT';
        end if;
    end
$$;