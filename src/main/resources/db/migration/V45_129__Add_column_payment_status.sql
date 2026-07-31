do
$$
    begin
        if not exists (
            select
            from pg_type
            where typname = 'payment_status'
        ) then
            create type payment_status as enum (
                'VALIDATE',
                'INVALIDATE',
                'CREATED'
                );
        end if;
    end;
$$;

alter table payment add column if not exists "payment_status" payment_status;
