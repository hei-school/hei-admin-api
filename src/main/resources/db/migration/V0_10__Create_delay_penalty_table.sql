do
$$
begin
        if not exists(select from pg_type where typname = 'interest_timerate_type ') then
create type "interest_timerate_type" as enum ('DAILY');
end if;
end
$$;

create table if not exists "delay_penalty"
(
    id                varchar
    constraint user_pk primary key default uuid_generate_v4(),
    creation_datetime        Timestamp,
    interest_percent         int,
    interest_time_rate       interest_timerate_type,
    grace_delay int,
    applicability_delay_after_grace int
);
