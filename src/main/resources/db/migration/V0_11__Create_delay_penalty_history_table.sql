create extension if not exists "uuid-ossp";

create table if not exists "delay_penalty_history"
(
    id                varchar
        constraint delay_penalty_history_pk primary key                 default uuid_generate_v4(),
    delay_penalty_id           varchar                  not null
            constraint delay_penalty_history_delay_penalty_id_fk references "delay_penalty"(id),
    interest_percent               integer                  not null,
    time_frequency               integer                  not null,
    start_date               date ,
    end_date                date ,
    creation_date            timestamp with time zone
);
create index if not exists delay_penalty_history_start_date_end_date_index on "delay_penalty_history" (start_date,end_date);
