INSERT INTO stats_advanced_fees (stat_type, first_grade_count_monthly, second_grade_count_monthly, third_grade_count_monthly,first_grade_count_yearly, second_grade_count_yearly, third_grade_count_yearly, retake_exam_first_grade_count, retake_exam_second_grade_count, retake_exam_third_grade_count,work_study_count, monthly_count, yearly_count, mpbs_count,
                                 bank_transfer_count, unknown_frequency_count, unknown_grade_count, update_datetime,
                                 stat_start_date, stat_end_date, count_type)
VALUES ('LATE_COUNT'::advanced_fee_stats_type, 0, 0, 0, 0,0,0,0, 0, 0, 0, 0, 0, NULL, NULL, 0, 0, '2025-05-14', '2024-04-01',
        '2024-04-30', 'RECEIPT'::advanced_fee_stats_count_type),
       ('PENDING_COUNT'::advanced_fee_stats_type, 0, 0, 0, 0, 0,0,0,0, 0, 1, 0, 0, NULL, NULL, 1, 0, '2025-05-14', '2024-04-01',
        '2024-04-30', 'RECEIPT'::advanced_fee_stats_count_type),
       ('PAID_COUNT'::advanced_fee_stats_type, 186, 118, 84, 0, 0, 14,0,0,0, 20, 388, 2, 405, 3, 18, 0, '2025-05-14', '2024-04-01',
        '2024-04-30', 'RECEIPT'::advanced_fee_stats_count_type),
       ('TOTAL_COUNT'::advanced_fee_stats_type, 194, 118, 84, 0, 0, 0,0,0,0, 22, 396, 2, NULL, NULL, 20, 0, '2025-05-14',
        '2024-04-01', '2024-04-30', 'RECEIPT'::advanced_fee_stats_count_type);