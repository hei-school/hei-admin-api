insert into "user"
(id, first_name, last_name, email, ref, status, sex, birth_date, entrance_datetime, phone, address, "role", is_working_study, taken_working_study)
values ('student7_id', 'Working', 'One', 'test+working1@hei.school', 'STD29005', 'ENABLED', 'M',
        '2000-12-01',
        '2021-11-08T08:25:24.00Z', '0322411123', 'Adr 1', 'STUDENT', true, false),
       ('student8_id', 'Taken', 'Two', 'test+taken@hei.school', 'STD29006', 'ENABLED', 'F',
        '2000-12-02',
        '2021-11-09T08:26:24.00Z', '0322411124', 'Adr 2', 'STUDENT', false, true);