alter table stats_advanced_fees add column stat_date date default current_date;
alter table stats_advanced_fees add column update_datetime date default current_timestamp;