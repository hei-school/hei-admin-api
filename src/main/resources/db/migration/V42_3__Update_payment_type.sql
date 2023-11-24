do
$$
    begin
        if exists(select from pg_type where typname = 'payment_type') then
        alter type "payment_type" add value 'BANK_TRANSFER';
    end if;
end
$$;