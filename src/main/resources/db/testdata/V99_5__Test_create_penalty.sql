INSERT INTO "delay_penalty"
(id,interest_percent, interest_timerate, grace_delay, applicability_delay_after_grace, creation_datetime, student_id)
VALUES
    ('delay_penalty1_id',2, 'DAILY', 5, 15, '2022-11-08T08:25:24.00Z', null),
    ('delay_penalty2_id',3, 'DAILY', 5, 15, '2022-11-08T08:25:24.00Z', 'student2_id');