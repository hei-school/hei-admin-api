do
$$
    begin
        if not exists(select from pg_type where typname = 'fee_type') then
            create type "fee_type" as enum ('TUITION', 'HARDWARE');
        end if;
    end
$$;

create table if not exists "fee"
(
    id                varchar
        constraint fee_pk primary key default uuid_generate_v4(),
    user_id           varchar                  not null
        constraint fee_user_id_fk references "user"(id),
    type              fee_type                 not null,
    total_amount      integer                  not null,
    comment           varchar                  not null,
    creation_datetime timestamp with time zone not null default now(),
    due_datetime      timestamp with time zone not null
);
create index if not exists fee_user_id_index on "fee" (user_id);
