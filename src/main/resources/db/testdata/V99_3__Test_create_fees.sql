insert into "fee"
(id, user_id, type, comment, total_amount, remaining_amount, status, creation_datetime, updated_at, due_datetime,
 remaining_amount_with_interest, total_amount_with_interest)
values ('fee1_id', 'student1_id', 'TUITION', 'Comment', 5000, 0, 'PAID', '2021-11-08T08:25:24.00Z',
        '2023-02-08T08:30:24.00Z', '2021-12-08T08:25:24.00Z', 0, 5000),
       ('fee2_id', 'student1_id', 'HARDWARE', 'Comment', 5000, 0, 'PAID', '2021-11-10T08:25:24.00Z',
        '2023-02-08T08:30:24.00Z', '2021-12-10T08:25:24.00Z', 0, 5000),
       ('fee3_id', 'student1_id', 'TUITION', 'Comment', 5000, 5000, 'LATE', '2022-12-08T08:25:24.00Z',
        '2023-02-08T08:30:24.00Z', '2021-12-09T08:25:24.00Z', 5000, 5000),
       ('fee4_id', 'student2_id', 'TUITION', 'Comment', 5000, 5000, 'LATE', '2021-11-08T08:25:24.00Z',
        '2023-02-08T08:30:24.00Z', '2021-12-09T08:25:25.00Z', 5000, 5000),
       ('fee5_id', 'student2_id', 'HARDWARE', 'Comment', 5000, 5000, 'LATE', '2021-11-08T08:25:24.00Z',
        '2023-02-08T08:30:24.00Z', '2021-12-08T08:25:25.00Z', 5000, 5000),
       ('fee6_id', 'student1_id', 'TUITION', 'Comment', 5000, 5000, 'LATE', '2021-11-08T08:25:24.00Z',
        '2023-02-08T08:30:24.00Z', '2021-12-09T08:25:25.00Z', 5000, 5000),
       ('fee7_id', 'student1_id', 'TUITION', 'Comment', 15000, 5000, 'LATE', '2023-02-08T08:25:24.00Z',
        '2023-03-08T08:30:24Z', '2023-03-26T08:25:24.00Z', 5000, 15000);