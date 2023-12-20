do
$$
    begin
        if not exists(select from pg_type where typname = 'academic_stream') then
            create type "academic_stream" as enum ('COMMON_CORE', 'EL', 'TN');
        end if;
    end
$$;