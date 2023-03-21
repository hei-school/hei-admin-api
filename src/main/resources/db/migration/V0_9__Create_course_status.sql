do
$$
    begin
        if not exists(select from pg_type where typname = 'course_status') then
            create type "course_status" as enum ('LINKED', 'UNLINKED');
        end if;
    end
$$;