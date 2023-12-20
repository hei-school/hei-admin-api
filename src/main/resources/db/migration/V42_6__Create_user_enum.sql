do
$$
    begin
        if not exists(select from pg_type where typname = 'specialization_channel') then
            create type "specialization_channel" as enum ('COMMON_CORE', 'EL', 'TN');
        end if;
    end
$$;