create table if not exists "promotion"
(
    id varchar
    constraint promotion_pk primary key default uuid_generate_v4
(
),
    promotion_begin date not null,
    promotion_end date not null,
    promotion_range varchar not null
    );

create index if not exists promotion_range_index on "promotion" (promotion_range);