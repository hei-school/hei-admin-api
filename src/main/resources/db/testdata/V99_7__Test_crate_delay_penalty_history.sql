insert into delay_penalty_history
(id, delay_penalty_id, interest_percent, time_frequency, start_date, end_date,creation_date)
values
    ('delay_history1_id','delay_penalty1_id',2,1,'2023-03-08','2023-03-10','2023-03-08T08:30:24.00Z'),
    ('delay_history2_id','delay_penalty1_id',5,1,'2023-03-10','2023-03-16','2023-03-10T08:30:24.00Z'),
    ('delay_history3_id','delay_penalty1_id',5,3,'2023-03-16','2023-03-27','2023-03-16T08:30:24.00Z'),
    ('delay_history4_id','delay_penalty1_id',3,1,'2023-03-27','2023-03-27','2023-03-27T06:30:24.00Z'),
    ('delay_history5_id','delay_penalty1_id',4,1,'2023-03-27',null,'2023-03-27T08:30:24.00Z')
;