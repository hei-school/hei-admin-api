do
$$
    begin
        if not exists(select from pg_type where typname = 'cor_status') then
            create type "cor_status" as enum ('IN_PROGRESS', 'STAY', 'CANCELED', 'LEAVE', 'NO_SHOW');
        end if;
    end
$$;

CREATE TABLE public.cor
(
    id                   varchar NOT NULL default uuid_generate_v4(),
    creation_datetime    timestamp with time zone default now(),
    interview_datetime   timestamp with time zone,
    concerned_student_id varchar NOT NULL,
    description          varchar NOT NULL,
    status               cor_status NOT NULL default 'IN_PROGRESS'::cor_status,
    CONSTRAINT cor_pk PRIMARY KEY (id)
);