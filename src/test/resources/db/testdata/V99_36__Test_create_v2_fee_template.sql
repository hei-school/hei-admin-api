insert into "v2_fee_template" (id, label, type, category, creation_datetime)
values ('v2_fee_template1_id', 'Tuition 2026', 'TUITION', 'WORK_FEES', '2026-01-01T08:00:00.00Z');

insert into "v2_fee_template_content" (id, id_fee_template, label, amount, due_date)
values ('v2_fee_template_content1_id', 'v2_fee_template1_id', 'January', 5000, '2026-01-31'),
       ('v2_fee_template_content2_id', 'v2_fee_template1_id', 'February', 6000, '2026-02-28');
