insert into "user"
(id, first_name, last_name, email, ref, status, sex, birth_date, entrance_datetime, phone, address,
 "role")
values ('student4_id', 'Disable', 'One', 'test+disable1@hei.school', 'STD29001', 'DISABLED', 'M',
        '2000-12-01',
        '2021-11-08T08:25:24.00Z', '0322411123', 'Adr 1', 'STUDENT'),
       ('student5_id', 'Disable', 'Two', 'test+disable2@hei.school', 'STD29002', 'DISABLED', 'F',
        '2000-12-02',
        '2021-11-09T08:26:24.00Z', '0322411124', 'Adr 2', 'STUDENT'),
       ('teacher5_id', 'Disable', 'One', 'teacher+disable1@hei.school', 'TCR29001', 'DISABLED', 'M',
        '2000-12-01',
        '2021-11-08T08:25:24.00Z', '0322411123', 'Adr 1', 'TEACHER'),
       ('teacher6_id', 'Disable', 'Two', 'teacher+disable2@hei.school', 'TCR29002', 'DISABLED', 'F',
        '2000-12-02',
        '2021-11-09T08:26:24.00Z', '0322411124', 'Adr 2', 'TEACHER'),
       ('manager2_id', 'Disable', 'One', 'manager+disable1@hei.school', 'MGR29001', 'DISABLED', 'M',
        '2000-12-01',
        '2021-11-08T08:25:24.00Z', '0322411123', 'Adr 1', 'MANAGER');