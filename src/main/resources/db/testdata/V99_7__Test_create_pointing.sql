insert into "pointing"
    (id, pointing_datetime, datetime_course_enter, datetime_course_exit, is_late, is_missing, course_id, student_id, place)
values
    ('pointing1_id', '2021-11-08T08:25:24.00Z', '2021-11-08T09:00:24.00Z', '2021-11-08T12:00:00.00Z', false, false, 'course1_id', 'student1_id', 'Andraharo'),
    ('pointing2_id', '2021-08-08T08:25:24.00Z', '2021-08-08T09:00:24.00Z', '2021-08-08T12:00:00.00Z', false, false, 'course1_id', 'student1_id', 'Andraharo'),
    ('pointing3_id', '2021-11-08T09:25:24.00Z', '2021-11-08T09:00:24.00Z', '2021-11-08T12:00:00.00Z', true, false, 'course1_id', 'student2_id', 'Ivandry'),
    ('pointing4_id', '2021-11-08T10:30:24.00Z', '2021-11-08T010:00:24.00Z', '2021-11-08T12:00:00.00Z', true, false, 'course2_id', 'student2_id', 'Andraharo'),
    ('pointing5_id', '2022-01-08T08:15:00.00Z', '2022-01-08T08:00:00.00Z', '2022-01-08T10:00:00.00Z', true, false, 'course1_id', 'student2_id', 'Ivandry'),
    ('pointing6_id', null, '2021-11-08T09:00:24.00Z', '2021-11-08T12:00:00.00Z', false, true, 'course1_id', 'student3_id', null),
    ('pointing7_id', null, '2021-11-09T09:00:24.00Z', '2021-11-09T12:00:00.00Z', false, true, 'course1_id', 'student3_id', null);

