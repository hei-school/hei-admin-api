alter table "retake_exam"
    add column if not exists cancel_reason varchar,
    add column if not exists rejection_reason varchar;