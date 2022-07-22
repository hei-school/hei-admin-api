do
$$
    begin
        if exists(select from pg_type where typname = 'payment_type') then
           alter type "payment_type" add value 'MOBILE_MONEY';
        end if;
    end
$$;

alter table "payment" alter column comment drop not null;
