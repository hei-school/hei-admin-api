do
$$
    begin
        if not exists(select from pg_type where typname = 'job_progression') then
            create type "job_progression" as enum ('PENDING', 'PROCESSING', 'FINISHED');
        end if;

        if not exists(select from pg_type where typname = 'job_health') then
            create type "job_health" as enum ('UNKNOWN', 'SUCCEEDED', 'FAILED');
        end if;
    end
$$;

create table if not exists "fee_creation_task_status"(
    id varchar
        constraint fee_creation_task_status_pkey primary key default uuid_generate_v4(),
    id_fee_creation_task varchar not null
        constraint fee_creation_task_status_task_fkey references "fee_creation_task"(id),
    progression job_progression not null,
    health job_health not null,
    creation_datetime timestamp with time zone not null default now()
);

create table if not exists "fee_creation_job_status"(
    id varchar
        constraint fee_creation_job_status_pkey primary key default uuid_generate_v4(),
    id_fee_creation_job varchar not null
        constraint fee_creation_job_status_job_fkey references "fee_creation_job"(id),
    progression job_progression not null,
    health job_health not null,
    creation_datetime timestamp with time zone not null default now()
);

create table if not exists "fee_creation_job_statistics"(
    id varchar
        constraint fee_creation_job_statistics_pkey primary key default uuid_generate_v4(),
    id_fee_creation_job varchar not null
        constraint fee_creation_job_statistics_job_fkey references "fee_creation_job"(id)
        constraint fee_creation_job_statistics_job_unique unique,
    total_count int not null default 0,
    success_count int not null default 0,
    failure_count int not null default 0,
    update_datetime timestamp with time zone not null default now()
);
