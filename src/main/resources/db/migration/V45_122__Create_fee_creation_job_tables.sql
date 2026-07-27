create table if not exists "fee_creation_job"(
    id varchar
        constraint fee_creation_job_pkey primary key,
    id_fee_template varchar not null
        constraint fee_creation_job_template_fkey references "v2_fee_template"(id),
    creation_datetime timestamp with time zone not null default now(),
    end_datetime timestamp with time zone
);

create table if not exists "fee_creation_task"(
    id varchar
        constraint fee_creation_task_pkey primary key default uuid_generate_v4(),
    id_fee_creation_job varchar not null
        constraint fee_creation_task_job_fkey references "fee_creation_job"(id),
    student_ref varchar not null,
    message varchar,
    constraint fee_creation_task_unique_student_per_job
        unique (id_fee_creation_job, student_ref)
);
