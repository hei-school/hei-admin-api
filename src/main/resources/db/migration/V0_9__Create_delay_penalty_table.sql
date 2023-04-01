create table if not exists "delay_penalty"
(
    id                              varchar not null
        constraint pk_delay_penalty primary key                 default uuid_generate_v4(),
    interest_percent                integer not null,
    interest_timerate               varchar,
    creation_datetime               timestamp with time zone default now(),
    grace_delay                     integer not null,
    applicability_delay_after_grace integer not null
);
