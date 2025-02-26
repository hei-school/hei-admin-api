do
$$
    begin
        if not exists(select from pg_type where typname = 'advanced_fee_stats_type') then
            create type "advanced_fee_stats_type" as enum ('TOTAL_COUNT','PAID_COUNT','PENDING_COUNT','LATE_COUNT');
        end if;
        if not exists(select from pg_type where typname = 'payment_method') then
            create type "payment_method" as enum ('MPBS', 'BANK');
        end if;
    end
$$;

CREATE TABLE if not exists "stats_advanced_fees"
(
    id                  varchar
        constraint advanced_fee_stat_pk primary key       default uuid_generate_v4(),
    fee_id              character varying        NOT NULL
        constraint fee_adv_stats_fk references "fee" (id),
    first_grade_count   bigint                   NOT NULL DEFAULT 0,
    second_grade_count  bigint                   NOT NULL DEFAULT 0,
    third_grade_count   bigint                   NOT NULL DEFAULT 0,
    remedial_fees_count bigint                   NOT NULL DEFAULT 0,
    work_study_count    bigint                   NOT NULL DEFAULT 0,
    monthly_count       bigint                   not null default 0,
    yearly_count        bigint                   not null default 0,
    mpbs_count          bigint                   not null default 0,
    bank_transfer_count bigint                   not null default 0,
    insert_datetime     timestamp with time zone not null default now(),
    payment_method      payment_method,
    stat_type           advanced_fee_stats_type  NOT NULL DEFAULT 'TOTAL_COUNT'::advanced_fee_stats_type
);



