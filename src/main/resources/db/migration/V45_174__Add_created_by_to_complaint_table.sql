ALTER TABLE complaint
    ADD COLUMN IF NOT EXISTS created_by_id VARCHAR REFERENCES "user" (id);

UPDATE complaint
SET created_by_id = student_id
WHERE created_by_id IS NULL;

ALTER TABLE complaint
    ALTER COLUMN created_by_id SET NOT NULL;
