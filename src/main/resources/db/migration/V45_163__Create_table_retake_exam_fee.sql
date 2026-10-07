create table if not exists "retake_exam_fee" (
    id varchar constraint retake_exam_fee_pk primary key default uuid_generate_v4(),
    retake_exam_id varchar not null constraint retake_exam_fee_retake_exam_fk references "retake_exam"(id),
    fee_id varchar not null constraint retake_exam_fee_fee_fk references "fee"(id)
);
create unique index if not exists retake_exam_fee_retake_exam_id_uindex on "retake_exam_fee" (retake_exam_id);
