CREATE TABLE IF NOT EXISTS complaint_attachment
(
    id             VARCHAR CONSTRAINT complaint_attachment_pk PRIMARY KEY DEFAULT uuid_generate_v4(),
    complaint_id   VARCHAR REFERENCES complaint (id)                      NOT NULL,
    file_name      VARCHAR                                                NOT NULL,
    file_path      VARCHAR                                                NOT NULL,
    content_type   VARCHAR                                                NOT NULL,
    uploaded_by_id VARCHAR REFERENCES "user" (id)                         NOT NULL,
    created_at     TIMESTAMP WITH TIME ZONE DEFAULT now()
);
