do
$$
    begin
        if not exists(select from pg_type where typname = 'specialization_field') then
            create type "specialization_field" as enum ('COMMON_CORE', 'EL', 'TN');
        end if;
    end
$$;