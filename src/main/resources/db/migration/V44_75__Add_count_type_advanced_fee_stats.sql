do
$$
    begin
        if not exists(select from pg_type where typname = 'advanced_fee_stats_count_type') then
            create type "advanced_fee_stats_count_type" as enum ('ACCOUNTING', 'RECEIPT');
        end if;
    end
$$;

alter table stats_advanced_fees add column count_type advanced_fee_stats_count_type not null default 'RECEIPT'::advanced_fee_stats_count_type;
