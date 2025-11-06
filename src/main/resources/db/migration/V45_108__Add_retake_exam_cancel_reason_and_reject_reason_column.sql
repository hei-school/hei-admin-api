alter table "retake_exam" add column if not exists cancel_reason varchar;
alter table "retake_exam" add column if not exists reject_reason varchar;
