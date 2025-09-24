CREATE TABLE public.cor
(
    id                   varchar NOT NULL,
    creation_datetime    timestamp with time zone default now(),
    interview_datetime   timestamp with time zone,
    concerned_student_id varchar NOT NULL,
    description          varchar NOT NULL,
    CONSTRAINT cor_pk PRIMARY KEY (id)
);