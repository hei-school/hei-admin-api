insert into "retake_exam" (id, session_id, course_id, student_id, registration_date, status)
values('retake_exam1_id', 'session1_id', 'course1_id', 'student1_id', NOW(),'CANCELED'),
      ('retake_exam2_id', 'session2_id', 'course2_id', 'student2_id', NOW(), 'TO_CANCEL'),
      ('retake_exam3_id', 'session2_id', 'course3_id', 'student3_id', NOW(), 'REGISTERED'),
('retake_exam4_id', 'session2_id', 'course1_id', 'student2_id', NOW(), 'CANCELED');
