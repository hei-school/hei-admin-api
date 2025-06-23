ALTER TABLE stats_advanced_fees ADD COLUMN stat_start_date date not null default current_date;
ALTER TABLE stats_advanced_fees ADD COLUMN stat_end_date date not null default current_date;
