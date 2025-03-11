alter table stats_advanced_fees add column stat_date date default current_date;
alter table stats_advanced_fees rename column creation_datetime to update_datetime;