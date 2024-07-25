do
$$
begin
    if not exists(select from pg_type where typname = 'professional_experience_type') then
create type "professional_experience_type" as enum ('WORKER_STUDENT', 'BUSINESS_OWNER', 'INTERN_STUDENT', 'INTERN_PROJECT');
end if;
end
$$;


alter table "work_document" add column if not exists professional_experience_type professional_experience_type default 'WORKER_STUDENT';