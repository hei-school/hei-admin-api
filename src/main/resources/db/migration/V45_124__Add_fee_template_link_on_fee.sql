alter table "fee"
    add column if not exists id_fee_template varchar
        constraint fee_v2_fee_template_fkey references "v2_fee_template"(id);

create index if not exists fee_student_template_idx
    on "fee" (user_id, id_fee_template);
