insert into "course_session"
("id", "course_assignment_id", "begin", "end")
values
    ('course_session1_id', 'course_assignment1_id', '2021-11-08T08:00:00.00Z', '2021-11-08T12:00:00.00Z'),
    ('course_session2_id', 'course_assignment4_id', '2021-08-08T15:00:00.00Z', '2021-08-08T17:00:00.00Z'),
    ('current_course_session_id', 'course_assignment5_id', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);