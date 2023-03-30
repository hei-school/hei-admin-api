create extension if not exists "uuid-ossp";

create table if not exists "interest_history"
(
    id                varchar
        constraint interest_history_pk primary key                 default uuid_generate_v4(),
    fee_id           varchar                  not null
            constraint interest_history_fee_id_fk references "fee"(id),
    interest_rate               integer                  not null,
    interest_time_rate               integer                  not null,
    interest_start               DATE                  not null,
    interest_end                DATE                  not null,
    is_active                    boolean     not null    default true
);
