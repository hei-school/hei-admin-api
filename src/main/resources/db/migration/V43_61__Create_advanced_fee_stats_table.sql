do
$$
    begin
        if not exists(select from pg_type where typname = 'advanced_fee_stats_type') then
            create type "advanced_fee_stats_type" as enum ('TOTAL_COUNT','PAID_COUNT','PENDING_COUNT','LATE_COUNT');
        end if;
    end
$$;

CREATE TABLE if not exists "stats_advanced_fees"
(
    id                  varchar
        constraint advanced_fee_stat_pk primary key       default uuid_generate_v4(),
    first_grade_count   bigint                   NOT NULL DEFAULT 0,
    second_grade_count  bigint                   NOT NULL DEFAULT 0,
    third_grade_count   bigint                   NOT NULL DEFAULT 0,
    remedial_fees_count bigint                   NOT NULL DEFAULT 0,
    work_study_count    bigint                   NOT NULL DEFAULT 0,
    monthly_count       bigint                   not null default 0,
    yearly_count        bigint                   not null default 0,
    mpbs_count          bigint,
    bank_transfer_count bigint,
    creation_datetime   timestamp with time zone not null default now(),
    stat_type           advanced_fee_stats_type  NOT NULL DEFAULT 'TOTAL_COUNT'::advanced_fee_stats_type
);