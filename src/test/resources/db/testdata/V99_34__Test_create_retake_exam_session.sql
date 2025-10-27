INSERT INTO retake_exam_session (id, title, date_from, date_to)
VALUES
    ('session1_id', 'session1', NOW() + interval '1 month', NOW() + interval '1 month 20 days'),
    ('session2_id', 'session2', NOW() + interval '2 months', NOW() + interval '2 months 20 days'),
    ('session3_id', 'session3', NOW() + interval '3 months', NOW() + interval '3 months 20 days');
