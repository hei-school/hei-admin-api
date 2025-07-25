do
$$
    begin
        if not exists(select from pg_type where typname = 'student_level') then
            create type "student_level" as enum ('L1', 'L2', 'L3', 'M1', 'M2');
        end if;
    end
$$;