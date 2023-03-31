insert into delay_penalty
    (id, interest_time_rate, interest_percent, grace_delay, applicability_delay_after_grace, creation_datetime,last_update_date)
values
    ('delay_penalty1_id','DAILY',2,5,10,'2023-03-08T08:30:24.00Z','2023-03-09T08:30:24.00Z'),
    ('delay_penalty2_id','DAILY',5,5,10,'2023-03-08T08:30:24.00Z','2023-03-15T08:30:24.00Z'),
    ('delay_penalty3_id','DAILY',5,5,10,'2023-03-08T08:30:24.00Z','2023-03-20T08:30:24.00Z'),
    ('delay_penalty4_id','DAILY',3,5,10,'2023-03-08T08:30:24.00Z','2023-03-27T08:30:24.00Z'),
    ('delay_penalty5_id','DAILY',4,5,10,'2023-03-08T08:30:24.00Z','2023-03-27T11:30:24.00Z')
    ;