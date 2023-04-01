create table if not exists "fee_history"
(
    id                varchar
    constraint fee_history_pk primary key default uuid_generate_v4(),
    fee_total        integer                  not null,
    paid         boolean           not null     default false,
    percentage             integer                  not null,
    user_id           varchar                  not null
    constraint fee_history_user_id_fk references "user"(id)
    );
create index if not exists fee_history_user_id_index on "fee_history" (user_id);