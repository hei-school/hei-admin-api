ALTER TABLE stats_advanced_fees DROP COLUMN remedial_fees_count;
ALTER TABLE stats_advanced_fees ADD COLUMN remedial_first_grade_count bigint not null default 0;
ALTER TABLE stats_advanced_fees ADD COLUMN remedial_second_grade_count bigint not null default 0;
ALTER TABLE stats_advanced_fees ADD COLUMN remedial_third_grade_count bigint not null default 0;
