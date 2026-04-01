ALTER TABLE stats_advanced_fees RENAME COLUMN first_grade_count TO first_grade_count_monthly ;
ALTER TABLE stats_advanced_fees RENAME COLUMN second_grade_count TO second_grade_count_monthly ;
ALTER TABLE stats_advanced_fees RENAME COLUMN third_grade_count TO third_grade_count_monthly ;
ALTER TABLE stats_advanced_fees ADD COLUMN first_grade_count_yearly INT NOT NULL DEFAULT 0;
ALTER TABLE stats_advanced_fees ADD COLUMN second_grade_count_yearly INT NOT NULL DEFAULT 0;
ALTER TABLE stats_advanced_fees ADD COLUMN third_grade_count_yearly INT NOT NULL DEFAULT 0;
