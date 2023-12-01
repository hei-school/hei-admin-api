do
$$
    begin
        if exists(select from pg_type where typname = 'user_status') then
            alter type "user_status" add value 'SUSPENDED';
    end if;
end
$$;