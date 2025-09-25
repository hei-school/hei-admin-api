ALTER TABLE "retake_exam_session"
    ADD COLUMN IF NOT EXISTS title varchar NOT NULL DEFAULT 'default session';
