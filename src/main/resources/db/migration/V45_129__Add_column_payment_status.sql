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

alter table payment add column if not exists "status" payment_status default 'VALIDATE';
alter table payment alter column status set not null;
update payment set status = 'VALIDATE' where status is null;
