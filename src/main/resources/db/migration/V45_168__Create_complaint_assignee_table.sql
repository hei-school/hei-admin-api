CREATE TABLE IF NOT EXISTS complaint_assignee
(
    id                 VARCHAR CONSTRAINT complaint_assignee_pk PRIMARY KEY DEFAULT uuid_generate_v4(),
    complaint_id       VARCHAR REFERENCES complaint (id)                    NOT NULL,
    user_id            VARCHAR REFERENCES "user" (id)                       NOT NULL,
    assigned_by_id     VARCHAR REFERENCES "user" (id),
    assigned_at        TIMESTAMP WITH TIME ZONE DEFAULT now()
);
