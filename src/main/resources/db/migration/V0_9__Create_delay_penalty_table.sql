do
$$
    begin
        if not exists(select from pg_type where typname = 'interest_timerate') then
            create type "interest_timerate" as enum ('DAILY');
        end if;
    end
$$;

create table if not exists "delay_penalty"
(
    id varchar constraint delay_penalty_pk primary key default uuid_generate_v4(),
    interest_percent integer not null,
    grace_delay integer not null,
    interest_timerate interest_timerate not null default 'DAILY',
    applicability_delay_after_grace integer not null,
    creation_datetime timestamp with time zone not null default now()
);

