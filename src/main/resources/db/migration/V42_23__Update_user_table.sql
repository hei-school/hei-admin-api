do
$$
begin
    if not exists(select from pg_type where typname = 'work_status') then
        create type "work_status" as enum ('HAVE_BEEN_WORKING', 'WILL_BE_WORKING', 'WORKING');
end if;
end
$$;

alter table "user" add column if not exists work_status work_status;