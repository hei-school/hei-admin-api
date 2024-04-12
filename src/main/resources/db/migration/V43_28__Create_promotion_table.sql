create table if not exists "promotion"(
    id varchar constraint promotion_pk primary key default uuid_generate_v4(),
    "name" varchar not null,
    "ref"               varchar                  not null
    constraint promotion_ref_unique unique,
    creation_datetime timestamp with time zone not null default now()
);