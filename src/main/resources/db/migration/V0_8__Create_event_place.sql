do
$$
    begin
        if not exists(select from pg_type where typname = 'event_type') then
            create type event_type as enum ('COURSES', 'EXAMS', 'CONFERENCES');
        end if;
        if not exists(select from pg_type where typname = 'event_status') then
            create type event_status as enum ('ENABLED', 'CANCEL');
        end if;
    end
$$;

create table if not exists "event"
(
    id                varchar
        constraint event_pk primary key default uuid_generate_v4(),
    event_type        event_type                not null,
    responsible_id    varchar                   not null,
    start             timestamp with time zone  not null,
    end               timestamp with time zone  not null,
    place_id          varchar                   not null,
    status            event_status              not null,
    constraint event_responsible_fk foreign key (responsible_id) references user(id),
    constraint event_place_fk foreign key (place_id) references user(id)
);
