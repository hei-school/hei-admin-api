ALTER TABLE "stats_advanced_fees" ADD COLUMN if NOT EXISTS student_insurance_first_grade_count bigint NOT NULL DEFAULT 0;
ALTER TABLE "stats_advanced_fees" ADD COLUMN if NOT EXISTS student_insurance_second_grade_count bigint NOT NULL DEFAULT 0;
ALTER TABLE "stats_advanced_fees" ADD COLUMN if NOT EXISTS student_insurance_third_grade_count bigint NOT NULL DEFAULT 0;
