do
$$
    begin
        if not exists(select from pg_type where typname = 'interest_time_rate') then
            create type "interest_time_rate" as enum('DAILY','WEEKLY','MONTHLY')
        end if;
    end
$$;

create table if not exists "delay_penalty"
(
    id varchar not null
    interest_percent    integer     not null,
    interest_timerate       interest_time_rate      not null,
    grace_delay     integer     not null,
    applicability_delay_after_grace     integer     not null,
    creation_datetime   integer     not null
);