ALTER TABLE student_badge
    ADD COLUMN IF NOT EXISTS academic_year VARCHAR;
ALTER TABLE student_badge
    ADD COLUMN IF NOT EXISTS expiration_datetime TIMESTAMP WITH TIME ZONE;

DELETE
FROM student_badge
WHERE academic_year IS NULL
   OR expiration_datetime IS NULL;

ALTER TABLE student_badge
    ALTER COLUMN academic_year SET NOT NULL;
ALTER TABLE student_badge
    ALTER COLUMN expiration_datetime SET NOT NULL;

DROP INDEX IF EXISTS student_badge_active_student_idx;
CREATE UNIQUE INDEX IF NOT EXISTS student_badge_active_student_year_idx
    ON student_badge (student_id, academic_year) WHERE revocation_datetime IS NULL;
