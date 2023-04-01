do
$$
    begin
        if not exists(select from pg_type where typname = 'delay_penalty_interest_timerate') then
            create type "delay_penalty_interest_timerate" as enum ('DAILY');
        end if;
    end
$$;

create table if not exists "delay_penalty"
(
    id                varchar
        constraint delay_penalty_pk primary key default uuid_generate_v4(),
    interest_percent           float                  not null,
    interest_timerate              delay_penalty_interest_timerate                 not null,
    grace_delay            integer    not null        default  0,
    applicability_delay_after_grace           integer,
    creation_datetime timestamp with time zone not null default now()
);
