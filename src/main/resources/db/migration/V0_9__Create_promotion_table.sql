create table if not exists "promotion" (
    id    varchar
    constraint promotion_pk primary key default uuid_generate_v4(),
    promotion_name varchar not null,
    promotion_range varchar not null
    );

create index if not exists promotion_name_index on "promotion" (promotion_name);