do
$$
    begin
        if not exists(select from pg_type where typname = 'reaction_enum') then
            create type "reaction_enum" as enum ('UNCHECK', 'CHECK');
        end if;
    end
$$;

create table if not exists announcement_reaction
(
    id               varchar
        constraint announcement_reaction_pk primary key default uuid_generate_v4(),
    announcement_id  varchar
        constraint announcement_reaction_announcement_fk references "announcement" (id),
    user_id          varchar
        constraint announcement_reaction_user_fk references "user" (id),
    update_date_time timestamp with time zone           default now(),
    reaction reaction_enum
);