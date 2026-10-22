DO
$$
    BEGIN
        IF NOT EXISTS(SELECT FROM pg_type WHERE typname = 'complaint_type') THEN
            CREATE TYPE "complaint_type" AS ENUM ('ACADEMIC', 'FINANCIAL');
        END IF;
        IF NOT EXISTS(SELECT FROM pg_type WHERE typname = 'complaint_status') THEN
            CREATE TYPE "complaint_status" AS ENUM ('CREATED', 'IN_PROGRESS', 'RESOLVED', 'REJECTED');
        END IF;
    END
$$;

CREATE TABLE IF NOT EXISTS complaint_routing
(
    id                 VARCHAR CONSTRAINT complaint_routing_pk PRIMARY KEY DEFAULT uuid_generate_v4(),
    type               complaint_type                                     NOT NULL,
    manager_id         VARCHAR REFERENCES "user" (id)                     NOT NULL,
    creation_datetime  TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE IF NOT EXISTS complaint
(
    id                 VARCHAR CONSTRAINT complaint_pk PRIMARY KEY        DEFAULT uuid_generate_v4(),
    type               complaint_type                                    NOT NULL,
    title              VARCHAR,
    description        VARCHAR,
    status             complaint_status                                  NOT NULL DEFAULT 'CREATED',
    student_id         VARCHAR REFERENCES "user" (id)                    NOT NULL,
    created_at         TIMESTAMP WITH TIME ZONE DEFAULT now(),
    reminder_sent_at   TIMESTAMP WITH TIME ZONE
);

CREATE TABLE IF NOT EXISTS complaint_assignee
(
    id                 VARCHAR CONSTRAINT complaint_assignee_pk PRIMARY KEY DEFAULT uuid_generate_v4(),
    complaint_id       VARCHAR REFERENCES complaint (id)                    NOT NULL,
    user_id            VARCHAR REFERENCES "user" (id)                       NOT NULL,
    assigned_by_id     VARCHAR REFERENCES "user" (id),
    assigned_at        TIMESTAMP WITH TIME ZONE DEFAULT now()
);

CREATE TABLE IF NOT EXISTS complaint_status_history
(
    id                 VARCHAR CONSTRAINT complaint_status_history_pk PRIMARY KEY DEFAULT uuid_generate_v4(),
    datetime           TIMESTAMP WITH TIME ZONE DEFAULT now(),
    status             complaint_status                                            NOT NULL,
    comment            TEXT,
    complaint_id       VARCHAR REFERENCES complaint (id)                           NOT NULL,
    changed_by_id      VARCHAR REFERENCES "user" (id)
);
