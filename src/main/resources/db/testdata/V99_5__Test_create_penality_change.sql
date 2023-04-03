insert into "create_delay_penalty_change"
(id, student_id, interest_percent, interest_timerate, grace_delay, applicability_delay_after_grace, creation_datetime, status)
values
    ('penality1_id',null, 2, 'DAILY', 5, 5, '2023-01-15T08:25:25.00Z', 'GLOBAL'),
    ('penality2_id', 'student1_id', 2, 'DAILY', 7, 5, '2023-01-15T08:25:25.00Z', 'SPECIFIC');
    2023-04-03 06:23:20.502091