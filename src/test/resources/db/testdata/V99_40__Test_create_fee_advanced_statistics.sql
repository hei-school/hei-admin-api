insert into stats_advanced_fees (stat_type, first_grade_count, second_grade_count, third_grade_count,
                                 remedial_fees_count,
                                 work_study_count, monthly_count, yearly_count, mpbs_count, bank_transfer_count,
                                 creation_datetime)
VALUES ('TOTAL_COUNT'::advanced_fee_stats_type, 1, 0, 1, 0, 1, 0, 0, 0, 0, '2021-12-13T00:00:00+00:00'),
       ('PAID_COUNT'::advanced_fee_stats_type, 1, 0, 0, 0, 0, 0, 0, 1, 0, '2021-12-13T00:00:00+00:00'),
       ('LATE_COUNT'::advanced_fee_stats_type, 0, 0, 1, 0, 1, 0, 0, 0, 0, '2021-12-13T00:00:00+00:00');
