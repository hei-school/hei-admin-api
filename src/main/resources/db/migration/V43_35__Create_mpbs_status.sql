do
$$
begin
        if not exists(select from pg_type where typname = 'mpbs_status') then
create type "mpbs_status" as enum ('PENDING', 'SUCCESS', 'FAILED');
end if;
end
$$;

alter table "mpbs" add column "status" mpbs_status default 'PENDING';
