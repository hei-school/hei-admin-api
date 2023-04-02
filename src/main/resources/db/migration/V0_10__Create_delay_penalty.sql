do
$$
    begin
        if not exists(select from pg_type where typname = 'interest_time_rate_type') then
            create type "interest_time_rate_type" as enum ('DAILY');
        end if;
    end
$$;

create table if not exists "delay_penalty"
(
    id                varchar
        constraint delay_penalty_pk primary key default uuid_generate_v4(),
    interest_time_rate                  interest_time_rate_type  not null,
    interest_percent                    integer                  not null,
    grace_delay                         integer                  not null,
    applicability_delay_after_grace     integer                  not null,
    creation_datetime                   timestamp with time zone default now()
);
