CREATE TABLE IF NOT EXISTS "retake_exam_session_student_level"(
    id      VARCHAR CONSTRAINT retake_exam_session_student_level_pk PRIMARY KEY DEFAULT uuid_generate_v4(),
    retake_exam_session_id VARCHAR REFERENCES "retake_exam_session" (id)         NOT NULL,
    "student_level" student_level
);