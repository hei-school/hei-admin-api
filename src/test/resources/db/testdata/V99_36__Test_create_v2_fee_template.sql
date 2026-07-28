insert into "v2_fee_template" (id, label, type, category, creation_datetime)
values ('v2_fee_template1_id', 'Tuition 2026', 'TUITION', 'WORK_FEES', '2026-01-01T08:00:00.00Z');

insert into "v2_fee_template_content" (id, id_fee_template, label, amount, due_date)
values ('v2_fee_template_content1_id', 'v2_fee_template1_id', 'January', 5000, '2026-01-31'),
       ('v2_fee_template_content2_id', 'v2_fee_template1_id', 'February', 6000, '2026-02-28');

-- students of their own, so that the fees these jobs create never shift what the other
-- integration tests assert on: the postgres container is shared by every test of a same fork
insert into "user"
(id, first_name, last_name, email, ref, status, sex, birth_date, entrance_datetime, phone, address,
 "role", birth_place, nic, longitude, latitude, high_school_origin)
values ('fee_job_student1_id', 'One', 'FeeJob', 'test+feejob1@hei.school', 'STD26001', 'ENABLED',
        'M', '2000-01-01', '2026-01-08T08:25:24.00Z', '0322400001', 'Adr 1', 'STUDENT', '', '',
        0.0, 0.0, 'Lycée Test'),
       ('fee_job_student2_id', 'Two', 'FeeJob', 'test+feejob2@hei.school', 'STD26002', 'ENABLED',
        'F', '2000-01-02', '2026-01-08T08:25:24.00Z', '0322400002', 'Adr 2', 'STUDENT', '', '',
        0.0, 0.0, 'Lycée Test'),
       ('fee_job_student3_id', 'Three', 'FeeJob', 'test+feejob3@hei.school', 'STD26003', 'ENABLED',
        'M', '2000-01-03', '2026-01-08T08:25:24.00Z', '0322400003', 'Adr 3', 'STUDENT', '', '',
        0.0, 0.0, 'Lycée Test'),
       ('fee_job_student4_id', 'Four', 'FeeJob', 'test+feejob4@hei.school', 'STD26004', 'ENABLED',
        'F', '2000-01-04', '2026-01-08T08:25:24.00Z', '0322400004', 'Adr 4', 'STUDENT', '', '',
        0.0, 0.0, 'Lycée Test'),
       ('fee_job_student5_id', 'Five', 'FeeJob', 'test+feejob5@hei.school', 'STD26005', 'ENABLED',
        'M', '2000-01-05', '2026-01-08T08:25:24.00Z', '0322400005', 'Adr 5', 'STUDENT', '', '',
        0.0, 0.0, 'Lycée Test'),
       ('fee_job_student6_id', 'Six', 'FeeJob', 'test+feejob6@hei.school', 'STD26006', 'ENABLED',
        'F', '2000-01-06', '2026-01-08T08:25:24.00Z', '0322400006', 'Adr 6', 'STUDENT', '', '',
        0.0, 0.0, 'Lycée Test');
