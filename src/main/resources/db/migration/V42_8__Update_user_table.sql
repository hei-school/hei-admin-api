alter table "user" drop column specialization_field;
alter table "user" add column if not exists specialization_field specialization_field default 'COMMON_CORE';