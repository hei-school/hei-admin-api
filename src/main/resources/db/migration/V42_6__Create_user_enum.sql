do
$$
    begin
        if not exists(select from pg_type where typname = 'academic_sector') then
            create type "academic_sector" as enum ('COMMON_CORE', 'EL', 'TN');
        end if;
    end
$$;