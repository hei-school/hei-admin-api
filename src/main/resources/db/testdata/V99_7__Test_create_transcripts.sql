insert into "transcript"
(id, user_id, semester, academic_year, is_definitive,  creation_datetime)
values
    ('transcript1_id', 'student1_id', 'S1', 2022, true, '2022-03-31T08:25:24.00Z'),
    ('transcript2_id', 'student1_id', 'S2', 2022, false, '2022-09-10T08:25:24.00Z'),
    ('transcript3_id', 'student2_id', 'S2', 2022, true, '2022-09-30T08:25:24.00Z'),
    ('transcript4_id', 'student2_id', 'S3', 2023, false, '2023-03-31T08:25:24.00Z');