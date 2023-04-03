insert into "user"
(id, first_name, last_name, email, ref, status, sex, birth_date, entrance_datetime, phone, address,
 "role", proper_grace_delay)
values ('student4_id', 'Ryan', 'Andria', 'test+ryan1@hei.school', 'STD21004', 'ENABLED', 'M',
        '2000-01-01',
        '2021-11-08T08:25:24.00Z', '0322411123', 'Adr 1', 'STUDENT', 5);

insert into "fee"
(id, user_id, type, comment,total_amount,  remaining_amount, status,creation_datetime, updated_at, due_datetime)
values
    ('fee7_id', 'student4_id', 'TUITION','Comment', 5000, 5000, 'LATE','2021-11-08T08:25:24.00Z','2023-02-08T08:30:24.00Z','2021-12-08T08:25:24.00Z');