CREATE TABLE IF NOT EXISTS complaint_status_history
(
    id                 VARCHAR CONSTRAINT complaint_status_history_pk PRIMARY KEY DEFAULT uuid_generate_v4(),
    datetime           TIMESTAMP WITH TIME ZONE DEFAULT now(),
    status             complaint_status                                            NOT NULL,
    comment            TEXT,
    complaint_id       VARCHAR REFERENCES complaint (id)                           NOT NULL,
    changed_by_id      VARCHAR REFERENCES "user" (id)
);
