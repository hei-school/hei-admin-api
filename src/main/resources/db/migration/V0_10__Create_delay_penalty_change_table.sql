do
$$
    begin
        if not exists(select from pg_type where typname = 'interest_timerate_type') then
            create type "interest_timerate_type" as enum ('DAILY');
        end if;
        if not exists(select from pg_type where typname = 'delay_grace_enum_type') then
            create type "delay_grace_enum_type" as enum ('GLOBAL','SPECIFIC');
        end if;
    end
$$;

create table if not exists "create_delay_penalty_change"
(
        id                               varchar
            constraint penality_change_pk primary key default uuid_generate_v4(),
        interest_percent                 integer           not null,
        interest_timerate                interest_timerate_type default 'DAILY',
        grace_delay                      integer           not null,
        applicability_delay_after_grace  integer            not null,
        creation_datetime                timestamp with time zone not null default now(),
        student_id                       varchar
                    constraint fee_student_id_fk references "user"(id),
        status                           delay_grace_enum_type not null
);