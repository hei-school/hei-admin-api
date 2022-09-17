create extension if not exists "uuid-ossp";
do
$$
begin
        if not exists(select from pg_type where typname = 'status') then
create type participant_status as enum ('EXPECTED', 'HERE', 'MISSING');
    end if;
end
$$;
create table if not exists "event_participant"
(
    id                varchar
    constraint event_participant_pk primary key                 default uuid_generate_v4(),
    "user"                varchar
        constraint event_user   references "user"(id),
    event                varchar
        constraint event_participant_fk_event references "event"(id),
    status           participant_status                 not null
);
