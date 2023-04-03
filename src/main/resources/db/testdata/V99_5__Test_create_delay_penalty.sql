insert into "delay_penalty"
(id, user_id, interest_percent, interest_timerate, grace_delay, applicability_delay_after_grace,
 creation_datetime)
values ('delay_penalty1_id', null, 2, 'DAILY', 5, 30, '2022-11-08T08:25:24.00Z'),
        ('delay_penalty2_id', 'student3_id', 2, 'DAILY', 10, 30, '2022-11-08T08:25:24.00Z');