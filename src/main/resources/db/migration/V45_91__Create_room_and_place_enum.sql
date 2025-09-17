do
$$
    begin
        if not exists(select from pg_type where typname = 'room_name') then
            create type "room_name" as enum ('ALGEBRE', 'PI', 'SIGMA', 'NP', 'B', 'UNKNOWN');
        end if;
    end
$$;

do
$$
    begin
        if not exists(select from pg_type where typname = 'place_name') then
            create type "place_name" as enum ('IVANDRY', 'ANDRAHARO');
        end if;
    end
$$;