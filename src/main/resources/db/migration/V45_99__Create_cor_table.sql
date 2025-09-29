DO
$$
    BEGIN
        IF NOT EXISTS(SELECT FROM pg_type WHERE typname = 'cor_status') THEN
            CREATE TYPE "cor_status" AS ENUM ('IN_PROGRESS', 'STAY', 'CANCELED', 'LEAVE', 'NO_SHOW');
        END IF;
    END
$$;

CREATE TABLE public.cor
(
    id                 VARCHAR                        NOT NULL DEFAULT uuid_generate_v4(),
    creation_datetime  TIMESTAMP WITH TIME ZONE                DEFAULT now(),
    interview_datetime TIMESTAMP with time ZONE,
    student_id         VARCHAR REFERENCES "user" (id) NOT NULL,
    description        VARCHAR                        NOT NULL,
    status             cor_status                     NOT NULL DEFAULT 'IN_PROGRESS'::cor_status,
    CONSTRAINT cor_pk PRIMARY KEY (id)
);