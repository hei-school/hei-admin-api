alter table "mpbs"
    add column if not exists last_vola_poll_datetime timestamp with time zone;
